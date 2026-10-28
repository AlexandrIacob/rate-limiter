#!/usr/bin/env python3

# This script goes through all the k6 result files (the --summary-export
# json files k6 creates after each run) and puts the important numbers
# into two CSV files, because reading 160 json files by hand is not
# realistic anymore.
#
# raw_results.csv        -> one row per single run (pattern + algorithm + repetition)
# summary_by_pattern.csv -> one row per pattern + algorithm, averaged over the 5 reps
#
# How accepted/rejected is read from a k6 file:
# Most scripts call check() on every request, so k6 saves a "checks" metric
# with passes (accepted, status 200) and fails (rejected, status 429).
# below-limit.js does not call check() at all (because traffic never goes
# above the limit there, so there was nothing to check), so that file has
# no "checks" key. For that one we use "http_req_failed" instead, where
# confusingly "passes" means the request WAS marked failed, and "fails"
# means it was NOT failed. So passes = rejected, fails = accepted there.
# Either way the numbers line up with what we already counted by hand
# during the experiment.

import os
import sys
import json
import csv

PATTERNS = [
    "below-limit",
    "at-limit",
    "above-limit",
    "short-burst",
    "boundary-reset",
    "boundary-reset-delayed",
    "repeated-bursts",
    "fairness",
]

ALGORITHMS = [
    "fixed-window",
    "sliding-window-log",
    "sliding-window-counter",
    "token-bucket",
]


def parse_filename(filename):
    # filename looks like "<pattern>-<algorithm>-<rep>.json"
    # we just try every known algorithm name and see if it fits
    stem = filename.replace(".json", "")

    for algo in ALGORITHMS:
        marker = "-" + algo + "-"
        idx = stem.find(marker)
        if idx == -1:
            continue

        pattern = stem[:idx]
        rep_text = stem[idx + len(marker):]

        if pattern in PATTERNS and rep_text.isdigit():
            return pattern, algo, int(rep_text)

    return None


def extract_row(filepath):
    f = open(filepath)
    data = json.load(f)
    f.close()

    metrics = data["metrics"]

    if "checks" in metrics:
        accepted = metrics["checks"]["passes"]
        rejected = metrics["checks"]["fails"]
    else:
        # below-limit.js case, see comment at the top of the file
        hrf = metrics["http_req_failed"]
        rejected = hrf["passes"]
        accepted = hrf["fails"]

    total_checked = accepted + rejected
    if total_checked > 0:
        accept_rate = accepted / total_checked
    else:
        accept_rate = 0

    http_reqs = metrics.get("http_reqs", {})
    duration = metrics.get("http_req_duration", {})

    row = {
        "total_http_requests": http_reqs.get("count"),
        "request_rate": http_reqs.get("rate"),
        "accepted": accepted,
        "rejected": rejected,
        "total_checked": total_checked,
        "accept_rate": accept_rate,
        "latency_avg_ms": duration.get("avg"),
        "latency_med_ms": duration.get("med"),
        "latency_p90_ms": duration.get("p(90)"),
        "latency_p95_ms": duration.get("p(95)"),
        "latency_max_ms": duration.get("max"),
    }
    return row


def mean_of(values):
    total = 0
    count = 0
    for v in values:
        if v is not None:
            total = total + v
            count = count + 1
    if count == 0:
        return None
    return total / count


def std_of(values, mean):
    clean = []
    for v in values:
        if v is not None:
            clean.append(v)

    if len(clean) < 2:
        return 0.0

    total = 0
    for v in clean:
        total = total + (v - mean) ** 2

    variance = total / (len(clean) - 1)
    return variance ** 0.5


def main():
    if len(sys.argv) != 3:
        print("Usage: python3 consolidate_results.py <results_dir> <output_dir>")
        return

    results_dir = sys.argv[1]
    output_dir = sys.argv[2]

    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    all_files = sorted(os.listdir(results_dir))

    raw_rows = []
    skipped_files = []

    for filename in all_files:
        if not filename.endswith(".json"):
            continue

        parsed = parse_filename(filename)
        if parsed is None:
            skipped_files.append(filename)
            continue

        pattern, algorithm, rep = parsed
        full_path = os.path.join(results_dir, filename)

        try:
            row = extract_row(full_path)
        except Exception as e:
            skipped_files.append(filename + " (error: " + str(e) + ")")
            continue

        row["pattern"] = pattern
        row["algorithm"] = algorithm
        row["repetition"] = rep
        row["file"] = filename
        raw_rows.append(row)

    if len(skipped_files) > 0:
        print("Could not read " + str(len(skipped_files)) + " file(s):")
        for name in skipped_files:
            print("  - " + name)

    # sort nicely so the csv is easy to read / diff
    raw_rows.sort(key=lambda r: (r["pattern"], r["algorithm"], r["repetition"]))

    raw_fields = [
        "pattern", "algorithm", "repetition", "file",
        "total_http_requests", "request_rate",
        "accepted", "rejected", "total_checked", "accept_rate",
        "latency_avg_ms", "latency_med_ms", "latency_p90_ms",
        "latency_p95_ms", "latency_max_ms",
    ]

    raw_path = os.path.join(output_dir, "raw_results.csv")
    out_file = open(raw_path, "w", newline="")
    writer = csv.DictWriter(out_file, fieldnames=raw_fields)
    writer.writeheader()
    for row in raw_rows:
        writer.writerow(row)
    out_file.close()

    print("Wrote " + str(len(raw_rows)) + " rows to " + raw_path)

    # now build the summary: for every pattern + algorithm combo, average
    # the 5 repetitions. We just loop over every combo instead of some
    # fancy grouping, it's easier to follow.
    summary_rows = []

    for pattern in PATTERNS:
        for algorithm in ALGORITHMS:
            matching_rows = []
            for row in raw_rows:
                if row["pattern"] == pattern and row["algorithm"] == algorithm:
                    matching_rows.append(row)

            if len(matching_rows) == 0:
                continue

            accepted_values = [r["accepted"] for r in matching_rows]
            rejected_values = [r["rejected"] for r in matching_rows]
            rate_values = [r["accept_rate"] for r in matching_rows]
            latency_values = [r["latency_avg_ms"] for r in matching_rows]
            p95_values = [r["latency_p95_ms"] for r in matching_rows]

            accepted_mean = mean_of(accepted_values)
            rejected_mean = mean_of(rejected_values)
            rate_mean = mean_of(rate_values)
            latency_mean = mean_of(latency_values)
            p95_mean = mean_of(p95_values)

            summary_rows.append({
                "pattern": pattern,
                "algorithm": algorithm,
                "n_reps": len(matching_rows),
                "accepted_mean": round(accepted_mean, 2),
                "accepted_std": round(std_of(accepted_values, accepted_mean), 2),
                "rejected_mean": round(rejected_mean, 2),
                "rejected_std": round(std_of(rejected_values, rejected_mean), 2),
                "accept_rate_mean": round(rate_mean, 4),
                "accept_rate_std": round(std_of(rate_values, rate_mean), 4),
                "latency_avg_ms_mean": round(latency_mean, 3),
                "latency_avg_ms_std": round(std_of(latency_values, latency_mean), 3),
                "latency_p95_ms_mean": round(p95_mean, 3),
                "latency_p95_ms_std": round(std_of(p95_values, p95_mean), 3),
            })

    summary_fields = [
        "pattern", "algorithm", "n_reps",
        "accepted_mean", "accepted_std",
        "rejected_mean", "rejected_std",
        "accept_rate_mean", "accept_rate_std",
        "latency_avg_ms_mean", "latency_avg_ms_std",
        "latency_p95_ms_mean", "latency_p95_ms_std",
    ]

    summary_path = os.path.join(output_dir, "summary_by_pattern.csv")
    out_file = open(summary_path, "w", newline="")
    writer = csv.DictWriter(out_file, fieldnames=summary_fields)
    writer.writeheader()
    for row in summary_rows:
        writer.writerow(row)
    out_file.close()

    print("Wrote " + str(len(summary_rows)) + " rows to " + summary_path)


if __name__ == "__main__":
    main()
