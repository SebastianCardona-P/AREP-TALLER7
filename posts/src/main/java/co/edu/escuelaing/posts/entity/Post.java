package co.edu.escuelaing.posts.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a Post (tweet) in the system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "post")
public class Post {
    
    @Id
    private String id;
    
    private String userId;
    
    private String streamId;  // ID del stream al que pertenece este post
    
    private String content;
    
    private LocalDateTime createdAt;
    
    private int likes;
    
    private int retweets;
}
