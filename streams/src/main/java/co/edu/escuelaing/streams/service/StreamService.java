package co.edu.escuelaing.streams.service;

import co.edu.escuelaing.streams.entity.Stream;
import co.edu.escuelaing.streams.repository.StreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Stream business logic
 * Independent service - no dependencies on other services
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StreamService {
    
    private final StreamRepository streamRepository;
    
    /**
     * Get all streams ordered by creation date
     * @return List of all streams
     */
    public List<Stream> getAllStreams() {
        log.info("Fetching all streams");
        return streamRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get stream by ID
     * @param id the stream ID
     * @return Optional containing the stream if found
     */
    public Optional<Stream> getStreamById(String id) {
        log.info("Fetching stream with id: {}", id);
        return streamRepository.findById(id);
    }
    
    /**
     * Get all streams created by a user
     * @param userId the user ID
     * @return List of streams created by the user
     */
    public List<Stream> getStreamsByUserId(String userId) {
        log.info("Fetching streams created by user: {}", userId);
        return streamRepository.findByCreatedBy(userId);
    }
    
    /**
     * Create a new stream
     * Note: Validation of userId existence should be done at controller level
     * @param stream the stream to create
     * @return the created stream
     */
    @Transactional
    public Stream createStream(Stream stream) {
        log.info("Creating new stream: {} by user: {}", stream.getTitle(), stream.getCreatedBy());
        
        // Validate title
        if (stream.getTitle() == null || stream.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Stream title cannot be empty");
        }
        
        // Validate creator
        if (stream.getCreatedBy() == null || stream.getCreatedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("Creator user ID is required");
        }
        
        stream.setCreatedAt(LocalDateTime.now());
        stream.setLastUpdated(LocalDateTime.now());
        
        return streamRepository.save(stream);
    }
    
    /**
     * Update an existing stream
     * @param id the stream ID
     * @param streamDetails the updated stream details
     * @return the updated stream
     */
    @Transactional
    public Stream updateStream(String id, Stream streamDetails) {
        log.info("Updating stream with id: {}", id);
        
        Stream stream = streamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stream not found with id: " + id));
        
        if (streamDetails.getTitle() != null && !streamDetails.getTitle().trim().isEmpty()) {
            stream.setTitle(streamDetails.getTitle());
        }
        
        if (streamDetails.getDescription() != null) {
            stream.setDescription(streamDetails.getDescription());
        }
        
        stream.setLastUpdated(LocalDateTime.now());
        
        return streamRepository.save(stream);
    }
    
    /**
     * Update the lastUpdated timestamp of a stream
     * Useful when a new post is added to the stream
     * @param id the stream ID
     * @return the updated stream
     */
    @Transactional
    public Stream updateStreamTimestamp(String id) {
        log.info("Updating timestamp for stream: {}", id);
        
        Stream stream = streamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stream not found with id: " + id));
        
        stream.setLastUpdated(LocalDateTime.now());
        
        return streamRepository.save(stream);
    }
    
    /**
     * Delete a stream
     * Note: This does not delete associated posts
     * Posts should be deleted separately or handled at controller level
     * @param id the stream ID
     */
    @Transactional
    public void deleteStream(String id) {
        log.info("Deleting stream with id: {}", id);
        
        if (!streamRepository.existsById(id)) {
            throw new IllegalArgumentException("Stream not found with id: " + id);
        }
        
        streamRepository.deleteById(id);
    }
}
