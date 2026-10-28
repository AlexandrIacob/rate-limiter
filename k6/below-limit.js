import http from 'k6/http';

export const options = {
  scenarios: {
    below_limit: {
      executor: 'constant-arrival-rate',
      rate: 5,
      timeUnit: '1s',
      duration: '30s',
      preAllocatedVUs: 10,
    },
  },
};

export default function () {
  http.get('http://localhost:8080/ping', {headers: {'X-Client-Id': `below-limit-client-${__ENV.REP}`}});
}