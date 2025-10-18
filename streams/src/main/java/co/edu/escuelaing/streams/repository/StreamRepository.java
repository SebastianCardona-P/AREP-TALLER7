package co.edu.escuelaing.streams.repository;

import co.edu.escuelaing.streams.entity.Stream;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Stream entity
 */
@Repository
public interface StreamRepository extends MongoRepository<Stream, String> {
    
    /**
     * Find streams by creator user ID
     * @param createdBy the user ID who created the stream
     * @return List of streams created by the user
     */
    List<Stream> findByCreatedBy(String createdBy);
    
    /**
     * Find all streams ordered by creation date descending
     * @return List of streams
     */
    List<Stream> findAllByOrderByCreatedAtDesc();
}
