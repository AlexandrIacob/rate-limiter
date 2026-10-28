import http from 'k6/http';
import {check} from 'k6';

export const options = {
  scenarios: {
    at_limit: {
      executor: 'constant-arrival-rate',
      rate: 10,
      timeUnit: '1s',
      duration: '30s',
      preAllocatedVUs: 10,
    },
  },
};

export default function () {
  const res = http.get('http://localhost:8080/ping', {headers: {'X-Client-Id': `at-limit-client-${__ENV.REP}`}});
  check(res, {
    'status is 200' : (r) => r.status === 200,
  });
}