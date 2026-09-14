package com.alexandr.ratelimiter.ratelimiter;

/** The interface for rate limiting requests, it will receive an object
 * of the String type, which is the client key, and based on it,
 * it will determine whether to allow the request or not.

*/
public interface RateLimiter {

    boolean allowRequest(String clientKey);

}
