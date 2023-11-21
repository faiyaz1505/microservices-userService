package com.lcwd.user.service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcwd.user.service.entities.Hotel;
import com.lcwd.user.service.entities.Ratings;
import com.lcwd.user.service.entities.User;
import com.lcwd.user.service.exception.ResourceNotFoundException;
import com.lcwd.user.service.repositories.UserRepository;
import com.lcwd.user.service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    private Logger logger= LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User saveUser(User user) {
        String randomUserId=UUID.randomUUID().toString();
        user.setUserId(randomUserId);

//      creating post request using rest template

//        List<Ratings> rating=user.getRatings();
//        for (Ratings rating1:rating) {
//            Ratings ratings=new Ratings();
//            ratings.setUserId(randomUserId);
//            restTemplate.postForLocation("http://localhost:8083/ratings",rating1);
//        }



        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        List<User> userList=userRepository.findAll();
        List<User> finalList=new ArrayList<>();
        for (User user1:userList) {
            ArrayList<Ratings> forObject=restTemplate.getForObject("http://localhost:8083/ratings/users/"+user1.getUserId(), ArrayList.class);
            user1.setRatings(forObject);
            finalList.add(user1);
        }
        return finalList;
    }

    @Override
    public User getUser(String userId) {
        User user=userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("user with given id is not found on server ! : "+userId));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode forObject=restTemplate.getForObject("http://localhost:8083/ratings/users/"+user.getUserId(), JsonNode.class);
        List<Ratings>  result= mapper.convertValue(forObject, new TypeReference<List<Ratings>>() {});
        logger.info("{}",forObject);

        List<Ratings> ratingsList=new ArrayList<>();
        for (Ratings rating:result) {
            Ratings ratings=new Ratings();
            ratings.setRatingId(rating.getRatingId());
            ratings.setUserId(rating.getUserId());
            ratings.setRating(rating.getRating());
            ratings.setHotelId(rating.getHotelId());
            ratings.setFeedback(rating.getFeedback());
            Hotel hotelObject=restTemplate.getForObject("http://localhost:8082/hotels/"+rating.getHotelId(), Hotel.class);
            ratings.setHotel(hotelObject);
            ratingsList.add(ratings);

        }




        user.setRatings(ratingsList);
        return user;
    }
}
