import http from 'k6/http';
import {check} from 'k6';

export const options = {
    scenarios: {
        aggressive: {
            executor: 'constant-arrival-rate',
            rate: 30,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 10,
            exec: 'aggressive',
        },

        normal_a: {
            executor: 'constant-arrival-rate',
            rate: 5,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 5,
            exec: 'normalA',
        },

        normal_b: {
            executor: 'constant-arrival-rate',
            rate: 5,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 5,
            exec: 'normalB',
        },

        normal_c: {
            executor: 'constant-arrival-rate',
            rate: 5,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 5,
            exec: 'normalC',
        },

    },
};

export function aggressive() {
    
    const res = http.get('http://localhost:8080/ping', {headers: {'X-Client-Id': `aggressive-${__ENV.REP}`}});
    check(res, {
        'aggressive status is 200' : (r) => r.status === 200,
    });
}

export function normalA() {
  const res = http.get('http://localhost:8080/ping', { headers: { 'X-Client-Id': `normal-a-${__ENV.REP}` } });
  check(res, { 
        'normal-a status is 200': (r) => r.status === 200 
    });
}

export function normalB() {
  const res = http.get('http://localhost:8080/ping', { headers: { 'X-Client-Id': `normal-b-${__ENV.REP}` } });
  check(res, {
        'normal-b status is 200': (r) => r.status === 200 
    });
}

export function normalC() {
  const res = http.get('http://localhost:8080/ping', { headers: { 'X-Client-Id': `normal-c-${__ENV.REP}` } });
  check(res, {
        'normal-c status is 200': (r) => r.status === 200
    });
}
