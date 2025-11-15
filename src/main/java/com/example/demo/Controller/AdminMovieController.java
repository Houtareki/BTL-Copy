package com.example.demo.Controller;

import com.example.demo.Entity.Genre;
import com.example.demo.Entity.User;
import com.example.demo.Service.GenreService;
import com.example.demo.Service.MovieService;
import com.example.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entity.Movie;
import com.example.demo.Model.ResponseModel.CustomData;
import com.example.demo.Model.ResponseModel.CustomResponse;
import com.example.demo.Service.MovieServiceAdmin;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminMovieController {

    @Autowired
    private MovieServiceAdmin movieServiceAdmin;
    @Autowired
    private MovieService  movieService;
    @Autowired
    private UserService userService;

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

    //---------------------------------------------------------------------

    private boolean isNotAdmin(int userId) {
        try {
            User user = userService.getUserById(userId);
            return user == null || user.getRole() != com.example.demo.Model.CommonModel.Role.ADMIN;
        }
        catch (Exception e) {
            return true;
        }
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.ok(
                new CustomResponse<>("Error", "Bạn không có quyền truy cập!", null)
        );
    }

    @GetMapping("/movies")
    @ResponseBody
    public ResponseEntity<?> getMovies(
            @RequestParam("userId") int userId,
            @RequestParam(defaultValue = "0") int pageNo) {

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<Movie> moviePage = movieService.getAllMovies(pageNo);
        return ResponseEntity.ok(new CustomResponse<>("Success", "Lấy danh sách phim thành công", new CustomData<>(moviePage)));
    }

    @PostMapping("/search/movies")
    @ResponseBody
    public ResponseEntity<?> searchMovies(
            @RequestParam("userId") int userId,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("keyword") String keyword) {

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<Movie> moviePage = movieService.searchMoviesByTitle(keyword, pageNo);
        return ResponseEntity.ok(new CustomResponse<>("Success", null, new CustomData<>(moviePage)));
    }

    @GetMapping("/get/movies")
    @ResponseBody
    public ResponseEntity<?> getMoviesDetails(
            @RequestParam("userId") int userId,
            @RequestParam("movieId") int movieId) {

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Movie movie = movieService.getMovieById(movieId);

        if (movie == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy phim", null));
        }
        return ResponseEntity.ok(new CustomResponse<>("Success", null, new CustomData<>(movie)));
    }

    @PutMapping("/update/movie")
    @ResponseBody
    public ResponseEntity<?> updateMovie(
            @RequestParam("userId") int userId,
            @RequestParam("movieId") int movieId,
            @RequestBody Movie movie) {

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Movie updatedMovie = movieService.updateMovie(movieId, movie);
        return ResponseEntity.ok(new CustomResponse<>("Success", "Cập nhật phim thành công", new CustomData<>(updatedMovie)));
    }

    @DeleteMapping("/delete/movie")
    @ResponseBody
    public ResponseEntity<?> deleteMovie(
            @RequestParam("userId") int userId,
            @RequestParam("movieId") int movieId) {

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        movieService.deleteMovie(movieId);
        return ResponseEntity.ok(new CustomResponse<>("Success", "Xóa phim thành công", null));
    }

    //-------------------------------Genre-----------------------------------
    @Autowired
    private GenreService genreService;

    @GetMapping("/genres")
    @ResponseBody
    public ResponseEntity<?> getGenres(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<Genre> genrePage = genreService.getAllGenre(pageNo);
        CustomData<Page<Genre>> data = new CustomData<>(genrePage);
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @GetMapping("/search/genres")
    @ResponseBody
    public ResponseEntity<?> searchGenres(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo,
            @RequestParam(name = "keyword") String keyword){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<Genre> genresPage = genreService.searchGenre(keyword, pageNo);
        CustomData<Page<Genre>> data = new CustomData<>(genresPage);
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @GetMapping("/genre")
    @ResponseBody
    public ResponseEntity<?> getGenreData(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "genreId") int genreId){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Optional<Genre> genre = genreService.getGenreById(genreId);
        if (genre.isEmpty()) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy thể loại", null));
        }

        CustomData<Genre> data = new CustomData<>(genre.get());
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @PostMapping("/add/genre")
    @ResponseBody
    public ResponseEntity<?> addGenre(
            @RequestParam(name = "userId") int userId,
            @RequestBody Genre genre) {

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Genre newGenre = genreService.addGenre(genre);

        if (newGenre == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Thêm thất bại", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", "Thêm thành công", null));
    }

    @PutMapping("/update/genre")
    @ResponseBody
    public ResponseEntity<?> updateGenre(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "genreId") int genreId,
            @RequestBody Genre genre){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Genre updatedGenre = genreService.updateGenre(genreId, genre);

        if (updatedGenre == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Cập nhật thất bại!", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", "Cập nhật thành công!", null));
    }

    @DeleteMapping("/delete/genre")
    @ResponseBody
    public ResponseEntity<?> deleteGenre(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "genreId") int genreId){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        boolean isDeleted = genreService.deleteGenre(genreId);

        if (!isDeleted) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Xóa thất bại!", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", "Xóa thành công!", null));
    }

}