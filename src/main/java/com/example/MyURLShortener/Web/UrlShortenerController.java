package com.example.MyURLShortener.Web;

import com.example.MyURLShortener.Services.UrlConverterService;
import com.example.MyURLShortener.common.Url;
import com.example.MyURLShortener.common.UrlNotFoundException;
import com.example.MyURLShortener.common.UrlValidator;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

@RestController
@RequestMapping(value = "/")
public class UrlShortenerController {

    @Autowired
    private RedisTemplate<String, Url> redisTemplate;

    @Autowired
    private UrlConverterService urlConverterService;

    @Value("${redis.ttl}")
    private long ttl;

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlShortenerController.class);

    private final Bucket bucket;

    public UrlShortenerController() {
        this.bucket = Bucket4j.builder()
                .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                .build();
    }

    @PostMapping(value = "shortener")
    public String shortenUrl(@RequestBody final String longUrl, HttpServletRequest request) throws ResponseStatusException {
        if (bucket.tryConsume(1)) {
            LOGGER.info("Received url to shorten: " + longUrl);
            if (UrlValidator.INSTANCE.validateURL(longUrl)) {
                String localUrl = request.getRequestURL().toString();
                String shortenedUrl = urlConverterService.shortenUrl(localUrl, longUrl);
                LOGGER.info("Shortened url to: " + shortenedUrl);
                return shortenedUrl;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid URL");
        }
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Request quota exceeded");
    }

    @GetMapping(value = "{id}")
    public RedirectView redirectUrl(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws UrlNotFoundException {
        LOGGER.debug("Received shortened url to redirect: " + id);
        String redirectUrlString = urlConverterService.getLongUrlFromID(id);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectUrlString);
        return redirectView;
    }
}
