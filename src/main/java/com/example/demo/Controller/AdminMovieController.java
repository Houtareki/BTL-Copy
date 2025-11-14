package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entity.Movie;
import com.example.demo.Model.ResponseModel.CustomData;
import com.example.demo.Model.ResponseModel.CustomResponse;
import com.example.demo.Service.MovieServiceAdmin; 

import java.util.List; 

@RestController 
@RequestMapping("/api/admin/movies") 
public class AdminMovieController {

    @Autowired
    private MovieServiceAdmin movieServiceAdmin; 

    @GetMapping
    public ResponseEntity<?> getAllMovies() {
        try {
            List<Movie> movies = movieServiceAdmin.getAllMovies(); 
            
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new CustomResponse<>("Error", "Không thể lấy danh sách phim: " + e.getMessage(), null)
            );
        }
    }
  
    // Thêm phim mới
    @PostMapping
    public ResponseEntity<?> addNewMovie(@RequestBody Movie movie) {
        try {
            Movie newMovie = movieServiceAdmin.addMovie(movie); 
            CustomData<Movie> data = new CustomData<>(newMovie);
            CustomResponse<Movie> response = new CustomResponse<>("Success", "Thêm phim mới thành công!", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new CustomResponse<>("Error", "Thêm phim thất bại: " + e.getMessage(), null)
            );
        }
    }
  
    // Sửa thông tin phim  
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable("id") int id, @RequestBody Movie movieDetails) {
        try {
            Movie updatedMovie = movieServiceAdmin.updateMovie(id, movieDetails); 
            CustomData<Movie> data = new CustomData<>(updatedMovie);
            CustomResponse<Movie> response = new CustomResponse<>("Success", "Cập nhật phim thành công!", data);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) { 
            return ResponseEntity.status(404).body(
                new CustomResponse<>("Error", e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new CustomResponse<>("Error", "Cập nhật phim thất bại: " + e.getMessage(), null)
            );
        }
    }
  
    // Xóa phim    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable("id") int id) {
        try {
            movieServiceAdmin.deleteMovie(id); 
            return ResponseEntity.ok(
                new CustomResponse<>("Success", "Xóa phim thành công!", null)
            );
        } catch (RuntimeException e) { 
             return ResponseEntity.status(404).body(
                new CustomResponse<>("Error", e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new CustomResponse<>("Error", "Xóa phim thất bại: " + e.getMessage(), null)
            );
        }
    }
}
