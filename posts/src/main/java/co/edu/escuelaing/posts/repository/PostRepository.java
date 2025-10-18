package co.edu.escuelaing.posts.repository;

import co.edu.escuelaing.posts.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Post entity
 */
@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    
    /**
     * Find all posts by a specific user
     * @param userId the user ID
     * @return List of posts
     */
    List<Post> findByUserId(String userId);
    
    /**
     * Find all posts by stream ID ordered by creation date descending
     * @param streamId the stream ID
     * @return List of posts in the stream
     */
    List<Post> findByStreamIdOrderByCreatedAtDesc(String streamId);
    
    /**
     * Find all posts ordered by creation date descending
     * @return List of posts
     */
    List<Post> findAllByOrderByCreatedAtDesc();
    
    /**
     * Find posts by user ordered by creation date descending
     * @param userId the user ID
     * @return List of posts
     */
    List<Post> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Count posts in a stream
     * @param streamId the stream ID
     * @return Number of posts
     */
    long countByStreamId(String streamId);
}
