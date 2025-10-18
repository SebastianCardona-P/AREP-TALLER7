package co.edu.escuelaing.streams.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a Stream (thread/timeline) in the system
 * Each stream can contain multiple posts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stream")
public class Stream {
    
    @Id
    private String id;
    
    private String title;  // Título del hilo/stream
    
    private String description;  // Descripción del hilo
    
    private String createdBy;  // ID del usuario que creó el stream
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastUpdated;
}
