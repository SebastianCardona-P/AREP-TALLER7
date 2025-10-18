package co.edu.escuelaing.posts.service;

import co.edu.escuelaing.posts.entity.Post;
import co.edu.escuelaing.posts.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Post business logic
 * Independent service - no dependencies on other services
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    
    private final PostRepository postRepository;
    
    private static final int MAX_POST_LENGTH = 140;
    
    /**
     * Get all posts ordered by creation date
     * @return List of all posts
     */
    public List<Post> getAllPosts() {
        log.info("Fetching all posts");
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get post by ID
     * @param id the post ID
     * @return Optional containing the post if found
     */
    public Optional<Post> getPostById(String id) {
        log.info("Fetching post with id: {}", id);
        return postRepository.findById(id);
    }
    
    /**
     * Get all posts by a user
     * @param userId the user ID
     * @return List of posts by the user
     */
    public List<Post> getPostsByUserId(String userId) {
        log.info("Fetching posts for user: {}", userId);
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get all posts in a stream
     * @param streamId the stream ID
     * @return List of posts in the stream
     */
    public List<Post> getPostsByStreamId(String streamId) {
        log.info("Fetching posts for stream: {}", streamId);
        return postRepository.findByStreamIdOrderByCreatedAtDesc(streamId);
    }
    
    /**
     * Count posts in a stream
     * @param streamId the stream ID
     * @return Number of posts in the stream
     */
    public long countPostsByStreamId(String streamId) {
        log.info("Counting posts in stream: {}", streamId);
        return postRepository.countByStreamId(streamId);
    }
    
    /**
     * Create a new post
     * Note: Validation of userId and streamId existence should be done at controller level
     * or before calling this method
     * @param post the post to create
     * @return the created post
     */
    @Transactional
    public Post createPost(Post post) {
        log.info("Creating new post for user: {} in stream: {}", post.getUserId(), post.getStreamId());
        
        // Validate post content length
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }
        
        if (post.getContent().length() > MAX_POST_LENGTH) {
            throw new IllegalArgumentException("Post content cannot exceed " + MAX_POST_LENGTH + " characters");
        }
        
        // Validate streamId is provided
        if (post.getStreamId() == null || post.getStreamId().trim().isEmpty()) {
            throw new IllegalArgumentException("Stream ID is required");
        }
        
        // Validate userId is provided
        if (post.getUserId() == null || post.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        post.setCreatedAt(LocalDateTime.now());
        post.setLikes(0);
        post.setRetweets(0);
        
        return postRepository.save(post);
    }
    
    /**
     * Update an existing post
     * @param id the post ID
     * @param postDetails the updated post details
     * @return the updated post
     */
    @Transactional
    public Post updatePost(String id, Post postDetails) {
        log.info("Updating post with id: {}", id);
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
        
        if (postDetails.getContent() != null) {
            if (postDetails.getContent().length() > MAX_POST_LENGTH) {
                throw new IllegalArgumentException("Post content cannot exceed " + MAX_POST_LENGTH + " characters");
            }
            post.setContent(postDetails.getContent());
        }
        
        return postRepository.save(post);
    }
    
    /**
     * Like a post
     * @param id the post ID
     * @return the updated post
     */
    @Transactional
    public Post likePost(String id) {
        log.info("Liking post with id: {}", id);
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
        
        post.setLikes(post.getLikes() + 1);
        return postRepository.save(post);
    }
    
    /**
     * Retweet a post
     * @param id the post ID
     * @return the updated post
     */
    @Transactional
    public Post retweetPost(String id) {
        log.info("Retweeting post with id: {}", id);
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
        
        post.setRetweets(post.getRetweets() + 1);
        return postRepository.save(post);
    }
    
    /**
     * Delete a post
     * @param id the post ID
     */
    @Transactional
    public void deletePost(String id) {
        log.info("Deleting post with id: {}", id);
        
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("Post not found with id: " + id);
        }
        
        postRepository.deleteById(id);
    }
}
