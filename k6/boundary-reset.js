import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    scenarios: {
        boundary_reset: {
            executor: 'shared-iterations',
            vus: 1,
            iterations: 1,
            maxDuration: '30s',
        },
    },
};

export default function() {
    http.get('http://localhost:8080/ping');
    sleep(9.8);
    for (let i = 0; i < 100; i++){
        const res = http.get('http://localhost:8080/ping');
        check(res, {
            'burst 1 status is 200' : (r) => r.status === 200,
        });
    }
    sleep(0.3)
    for (let i = 0; i < 100; i++) {
        const res = http.get('http://localhost:8080/ping');
        check(res, {
            'burst 2 status is 200': (r) => r.status === 200,
        });
}
}