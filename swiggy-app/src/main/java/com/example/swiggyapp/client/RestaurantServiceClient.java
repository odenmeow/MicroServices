package com.example.swiggyapp.client;

import com.example.swiggyapp.dto.OrderResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestaurantServiceClient {

    @Autowired
    // 由於 @LoadBalance ， 因此發出的請求會 執行負載平衡
    private RestTemplate template;

    public OrderResponseDTO fetchOrderStatus(String orderId) {
        return template.getForObject("http://RESTAURANT-SERVICE/restaurant/orders/status/" + orderId, OrderResponseDTO.class);
    }

//    @GetMapping("/callServiceB")
//    public ResponseEntity<String> callServiceB(@RequestHeader("Authorization") String token) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(token.substring(7)); // 確保去掉 'Bearer ' 前綴
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                "http://serviceB/api/resource", HttpMethod.GET, entity, String.class);
//
//        return ResponseEntity.ok(response.getBody());
//    }
}
