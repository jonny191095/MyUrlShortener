package com.example.MyURLShortener.Services;

import com.example.MyURLShortener.common.Url;
import com.example.MyURLShortener.common.UrlNotFoundException;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class UrlConverterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlConverterService.class);

    @Autowired
    private RedisTemplate<String, Url> redisTemplate;

    @Value("${redis.ttl}")
    private long timeToLive;

    public String shortenUrl(String localUrl, String longUrl) {
        LOGGER.info("Shortening {}", longUrl);
        String uniqueID = Hashing.murmur3_32().hashString(longUrl, Charset.defaultCharset()).toString();
        redisTemplate.opsForValue().set(uniqueID, new Url(longUrl, LocalDateTime.now()), timeToLive, TimeUnit.SECONDS);
        String baseString = formatLocalUrlFromShortener(localUrl);
        String shortenedUrl = baseString + uniqueID;
        return shortenedUrl;
    }

    public String getLongUrlFromId(String uniqueID) throws UrlNotFoundException {
        Url url = redisTemplate.opsForValue().get(uniqueID);
        if (url != null) {
            String longUrl = url.getUrl();
            LOGGER.info("Converting shortened URL back to {}", longUrl);
            return longUrl;
        }
        throw new UrlNotFoundException("Short URL Not Found");
    }

    // remove the last endpoint
    private String formatLocalUrlFromShortener(String localUrl) {
        String[] addressComponents = localUrl.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addressComponents.length - 1; i++) {
            sb.append(addressComponents[i]);
            sb.append('/');
        }
        return sb.toString();
    }
}
