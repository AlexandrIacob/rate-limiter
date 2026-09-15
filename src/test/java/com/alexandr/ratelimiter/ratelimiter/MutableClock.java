package com.alexandr.ratelimiter.ratelimiter;
import java.time.*;

public class MutableClock extends Clock{
     private Instant time;
     private ZoneId zone;
     public MutableClock (ZoneId initialZone, Instant time){
        this.zone = initialZone;
        this.time = time;
     }

     @Override 
     public ZoneId getZone(){
        return zone;
     }

     @Override 
     public Instant instant(){
        return time;
     }

     @Override 
     public Clock withZone(ZoneId zone){
        return new MutableClock(zone, time);
     }

     public Instant advance(Duration duration) {
        time = instant().plus(duration);
        return time;
        
     }

}
