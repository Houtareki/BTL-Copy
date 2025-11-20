package com.example.demo.Controller;

import com.example.demo.Entity.*;
import com.example.demo.Model.CommonModel.EpisodeAndMovieTitle;
import com.example.demo.Respository.MovieRepo;
import com.example.demo.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Model.ResponseModel.CustomData;
import com.example.demo.Model.ResponseModel.CustomResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    @Autowired
    private MovieRepo movieRepo;

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


    //----------------------------------Director-----------------------------------

    @Autowired
    private DirectorService directorService;

    @GetMapping("/directors")
    @ResponseBody
    public ResponseEntity<?> getAllDirectors(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<Director> directorPage = directorService.getAllDirectors(pageNo);
        CustomData<Page<Director>> data = new CustomData<>(directorPage);

        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @GetMapping("/search/directors")
    @ResponseBody
    public ResponseEntity<?> searchDirectors(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo,
            @RequestParam(name = "keyword") String keyword){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<Director> directorPage = directorService.searchDirectors(keyword, pageNo);
        CustomData<Page<Director>> data = new CustomData<>(directorPage);
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @GetMapping("/director")
    @ResponseBody
    public ResponseEntity<?> getDirectorData(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "directorId") int directorId){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Optional<Director> director = directorService.findById(directorId);

        if (director.isEmpty()) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy đạo diễn", null));
        }

        CustomData<Director> data = new CustomData<>(director.get());
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @PostMapping("/add/director")
    @ResponseBody
    public ResponseEntity<?> addDirector(
            @RequestParam(name = "userId") int userId,
            @RequestBody Director director){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Director newDirector = directorService.addDirector(director);

        if (newDirector == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Thêm thất bại.", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", null, null));
    }

    @PutMapping("/update/dỉrector")
    @ResponseBody
    public ResponseEntity<?> updateDirector(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "directorId") int directorId,
            @RequestBody Director director){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Director updatedDirector = directorService.updateDirector(directorId, director);
        if (updatedDirector == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Cập nhật thất bại.", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", null, null));
    }

    @DeleteMapping("/delete/director")
    @ResponseBody
    public ResponseEntity<?> deleteDirector(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "directorId") int directorId){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        boolean isDeleted = directorService.deleteDirector(directorId);

        if (!isDeleted) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Xóa thất bại.", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", null, null));
    }

    //------------------------------Actor---------------------------------

    @Autowired
    private ActorService actorService;

    @GetMapping("/actors")
    @ResponseBody
    public ResponseEntity<?> getAllActors(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<Actor> actorPage = actorService.getAllActors(pageNo);
        CustomData<Page<Actor>> data = new CustomData<>(actorPage);
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @GetMapping("/search/actors")
    @ResponseBody
    public ResponseEntity<?> searchActors(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo,
            @RequestParam(name = "keyword") String keyword){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<Actor> actorPage = actorService.searchActors(keyword, pageNo);
        CustomData<Page<Actor>> data = new CustomData<>(actorPage);
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @GetMapping("/actor")
    @ResponseBody
    public ResponseEntity<?> getActorData(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "actorId") int actorId){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Optional<Actor> actor = actorService.getActorById(actorId);

        if (actor.isEmpty()) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy diễn viên", null));
        }

        CustomData<Actor> data = new CustomData<>(actor.get());
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @PostMapping("/add/actor")
    @ResponseBody
    public ResponseEntity<?> addActor(
            @RequestParam(name = "userId") int userId,
            @RequestBody Actor actor){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Actor newActor = actorService.addActor(actor);
        if (newActor == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Thêm thất bại.", null));
        }
        return ResponseEntity.ok(new CustomResponse<>("Success", null, null));
    }

    @PutMapping("/update/actor")
    @ResponseBody
    public ResponseEntity<?> updateActor(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "actorId") int actorId,
            @RequestBody Actor actor){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Actor updatedActor = actorService.updateActor(actorId, actor);
        if (updatedActor == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Cập nhật thất bại.", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", null, null));
    }

    @DeleteMapping("/delete/actor")
    @ResponseBody
    public ResponseEntity<?> deleteActor(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "actorId") int actorId){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        boolean isDeleted = actorService.deleteActor(actorId);

        if (!isDeleted) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Xóa thất bại.", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", null, null));
    }

    //-------------------------Episode------------------
    @Autowired
    private EpisodeService episodeService;

    @GetMapping("/search/episodes")
    @ResponseBody
    public ResponseEntity<?> searchEpisodes(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo,
            @RequestParam(name = "keyword") String keyword){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<EpisodeAndMovieTitle> episodes = episodeService.searchEpisode(keyword, pageNo);
        CustomData<Page<EpisodeAndMovieTitle>> data = new CustomData<>(episodes);
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @GetMapping("/episode")
    @ResponseBody
    public ResponseEntity<?> getEpisode(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "episodeId") int episodeId){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Optional<Episode> episode = episodeService.getEpisodeById(episodeId);

        if (episode.isEmpty()) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy tập phim", null));
        }

        CustomData<Episode> data = new CustomData<>(episode.get());
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @GetMapping("/episodes/unlinked")
    @ResponseBody
    public ResponseEntity<?> getUnlinkedEpisodes(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<Episode> episodes = episodeService.getUnlinkedEpisodes(pageNo);
        CustomData<Page<Episode>> data = new CustomData<>(episodes);
        return ResponseEntity.ok(new CustomResponse<>("Success", null, data));
    }

    @PostMapping("/add/episode")
    @ResponseBody
    public ResponseEntity<?> addEpisode(
            @RequestParam(name = "userId") int userId,
            @RequestBody Episode episode){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Episode newEpisode = episodeService.addEpisode(episode);
        if (newEpisode == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Thêm thất bại!", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", "Thêm thành công!", null));
    }

    @PutMapping("/update/episode")
    @ResponseBody
    public ResponseEntity<?> updateEpisode(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "episodeId") int episodeId,
            @RequestBody Episode episode){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Episode updatedEpisode = episodeService.updateEpisode(episodeId, episode);
        if (updatedEpisode == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Cập nhật thất bại!", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", "Cập nhật thành công!", null));
    }

    @DeleteMapping("/delete/episode")
    @ResponseBody
    public ResponseEntity<?> deleteEpisode(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "episodeId") int episodeId){

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        boolean isDeleted = episodeService.deleteEpisode(episodeId);
        if (isDeleted) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Xóa thất bại!", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Error", "Xóa thành công!", null));
    }


    @GetMapping("/hot-movies")
    @ResponseBody
    public ResponseEntity<?> getHotMovies(
            @RequestParam("userId") int userId) {

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        try {
            int limit=20;
            List<Movie> seriesMovies = movieRepo.findTopSeriesMoviesByTotalViews(limit);

            List<Movie> fullMovies = movieRepo.findTopFullMoviesByViews(limit);

            Map<String, List<Movie>> data = new HashMap<>();
            data.put("series", seriesMovies);
            data.put("full", fullMovies);

            return ResponseEntity.ok(new CustomResponse<>("Success", "Lấy danh sách hot movies thành công", new CustomData<>(data)));
        } catch (Exception e) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Lỗi: " + e.getMessage(), null));
        }
    }
}
