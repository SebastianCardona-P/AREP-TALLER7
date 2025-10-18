package co.edu.escuelaing.streams.controller;

import co.edu.escuelaing.streams.entity.Stream;
import co.edu.escuelaing.streams.service.StreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Stream operations
 */
@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StreamController {
    
    private final StreamService streamService;
    
    /**
     * Get all streams
     * @return List of all streams ordered by date
     */
    @GetMapping
    public ResponseEntity<List<Stream>> getAllStreams() {
        log.info("GET /api/streams - Get all streams");
        return ResponseEntity.ok(streamService.getAllStreams());
    }
    
    /**
     * Get stream by ID
     * @param id the stream ID
     * @return the stream if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Stream> getStreamById(@PathVariable String id) {
        log.info("GET /api/streams/{} - Get stream by id", id);
        return streamService.getStreamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all streams created by a user
     * @param userId the user ID
     * @return List of streams created by the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Stream>> getStreamsByUserId(@PathVariable String userId) {
        log.info("GET /api/streams/user/{} - Get streams by user", userId);
        return ResponseEntity.ok(streamService.getStreamsByUserId(userId));
    }
    
    /**
     * Get stream with post count
     * @param id the stream ID
     * @return Stream info with post count
     */
    @GetMapping("/{id}/info")
    public ResponseEntity<?> getStreamInfo(@PathVariable String id) {
        log.info("GET /api/streams/{}/info - Get stream info with post count", id);
        
        return streamService.getStreamById(id)
                .map(stream -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("stream", stream);
                    return ResponseEntity.ok(info);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new stream
     * Validates that the creator user exists
     * @param stream the stream to create (must include createdBy)
     * @return the created stream
     */
    @PostMapping
    public ResponseEntity<?> createStream(@RequestBody Stream stream) {
        log.info("POST /api/streams - Create new stream");
        try {
           
            
            Stream createdStream = streamService.createStream(stream);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStream);
        } catch (IllegalArgumentException e) {
            log.error("Error creating stream: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Update an existing stream
     * @param id the stream ID
     * @param stream the updated stream details
     * @return the updated stream
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStream(@PathVariable String id, @RequestBody Stream stream) {
        log.info("PUT /api/streams/{} - Update stream", id);
        try {
            Stream updatedStream = streamService.updateStream(id, stream);
            return ResponseEntity.ok(updatedStream);
        } catch (IllegalArgumentException e) {
            log.error("Error updating stream: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Delete a stream
     * Note: This does not delete associated posts
     * @param id the stream ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStream(@PathVariable String id) {
        log.info("DELETE /api/streams/{} - Delete stream", id);
        try {
            streamService.deleteStream(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting stream: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
