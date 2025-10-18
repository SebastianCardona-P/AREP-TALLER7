package co.edu.escuelaing.posts.controller;

import co.edu.escuelaing.posts.entity.Post;
import co.edu.escuelaing.posts.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Post operations
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PostController {
    
    private final PostService postService;
    
    /**
     * Get all posts
     * @return List of all posts ordered by date
     */
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        log.info("GET /api/posts - Get all posts");
        return ResponseEntity.ok(postService.getAllPosts());
    }
    
    /**
     * Get post by ID
     * @param id the post ID
     * @return the post if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable String id) {
        log.info("GET /api/posts/{} - Get post by id", id);
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all posts by a user
     * @param userId the user ID
     * @return List of posts by the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable String userId) {
        log.info("GET /api/posts/user/{} - Get posts by user", userId);
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }
    
    /**
     * Get all posts in a stream
     * @param streamId the stream ID
     * @return List of posts in the stream
     */
    @GetMapping("/stream/{streamId}")
    public ResponseEntity<List<Post>> getPostsByStreamId(@PathVariable String streamId) {
        log.info("GET /api/posts/stream/{} - Get posts by stream", streamId);
        return ResponseEntity.ok(postService.getPostsByStreamId(streamId));
    }
    
    /**
     * Create a new post
     * Validates that both user and stream exist before creating the post
     * @param post the post to create (must include userId and streamId)
     * @return the created post
     */
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        log.info("POST /api/posts - Create new post");
        try {
            
            Post createdPost = postService.createPost(post);
            
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (IllegalArgumentException e) {
            log.error("Error creating post: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Update an existing post
     * @param id the post ID
     * @param post the updated post details
     * @return the updated post
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable String id, @RequestBody Post post) {
        log.info("PUT /api/posts/{} - Update post", id);
        try {
            Post updatedPost = postService.updatePost(id, post);
            return ResponseEntity.ok(updatedPost);
        } catch (IllegalArgumentException e) {
            log.error("Error updating post: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Like a post
     * @param id the post ID
     * @return the updated post
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable String id) {
        log.info("POST /api/posts/{}/like - Like post", id);
        try {
            Post likedPost = postService.likePost(id);
            return ResponseEntity.ok(likedPost);
        } catch (IllegalArgumentException e) {
            log.error("Error liking post: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Retweet a post
     * @param id the post ID
     * @return the updated post
     */
    @PostMapping("/{id}/retweet")
    public ResponseEntity<?> retweetPost(@PathVariable String id) {
        log.info("POST /api/posts/{}/retweet - Retweet post", id);
        try {
            Post retweetedPost = postService.retweetPost(id);
            return ResponseEntity.ok(retweetedPost);
        } catch (IllegalArgumentException e) {
            log.error("Error retweeting post: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Delete a post
     * @param id the post ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable String id) {
        log.info("DELETE /api/posts/{} - Delete post", id);
        try {
            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting post: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
