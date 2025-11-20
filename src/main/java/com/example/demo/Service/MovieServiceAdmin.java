package com.example.demo.Service;

import com.example.demo.Entity.Movie;
import com.example.demo.Respository.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MovieServiceAdmin {

    @Autowired
    private MovieRepo movieRepo;

    public List<Movie> getAllMovies() {

        return movieRepo.findAll();
    }

    //Thêm phim mới (cho Admin)

    public Movie addMovie(Movie movie) {
        movie.setViews(0);

        return movieRepo.save(movie);
    }
    // Sửa thông tin phim (cho Admin)

    public Movie updateMovie(int id, Movie movieDetails) {

        Movie existingMovie = movieRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy phim với id: " + id));

        existingMovie.setTitle(movieDetails.getTitle());
        existingMovie.setDescription(movieDetails.getDescription());
        existingMovie.setReleaseYear(movieDetails.getReleaseYear());
        existingMovie.setPosterUrl(movieDetails.getPosterUrl());
        existingMovie.setMovieStatus(movieDetails.getMovieStatus());
        existingMovie.setCountry(movieDetails.getCountry());
        existingMovie.setLanguage(movieDetails.getLanguage());
        existingMovie.setTrailerUrl(movieDetails.getTrailerUrl());
        existingMovie.setThumbUrl(movieDetails.getThumbUrl());

        return movieRepo.save(existingMovie);
    }
    // Xóa phim (cho Admin)

    public void deleteMovie(int id) {

        if (!movieRepo.existsById(id)) {
            throw new RuntimeException("Lỗi: Không tìm thấy phim với id: " + id);
        }

        movieRepo.deleteById(id);
    }
}
