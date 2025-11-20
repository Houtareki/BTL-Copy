package com.example.demo.Controller;

import com.example.demo.Model.CommonModel.UserForm;
import com.example.demo.Respository.CommentRepo;
import com.example.demo.Respository.MovieRepo;
import com.example.demo.Respository.UserRepo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entity.User;
import com.example.demo.Model.CommonModel.Role;
import com.example.demo.Model.ResponseModel.CustomData;
import com.example.demo.Model.ResponseModel.CustomResponse;
import com.example.demo.Service.UserService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/dashboard")
    public String getAdminDashboardPage() {
        return "admin";
    }

    private boolean isNotAdmin(int userId) {
        try {
            User user = userService.getUserById(userId);
            return user == null || user.getRole() != Role.ADMIN;
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

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<?> getUsers(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo) {

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<User> usersPage = userService.getAllUsers(pageNo);
        CustomData<Page<User>> data = new CustomData<>(usersPage);

        return ResponseEntity.ok(new CustomResponse<>("Success", "Lấy danh sách user thành công", data));
    }

    @GetMapping("/search/users")
    @ResponseBody
    public ResponseEntity<?> searchUsers(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo,
            @RequestParam(name = "keyword") String keyword) {

        if (isNotAdmin(userId)) {
            return unauthorized();
        }

        Page<User> usersPage = userService.searchUsers(keyword, pageNo);
        CustomData<Page<User>> data = new CustomData<>(usersPage);

        return ResponseEntity.ok(new CustomResponse<>("Success", "Tìm kiếm user thành công", data));
    }

    @GetMapping("/user")
    @ResponseBody
    public ResponseEntity<?> getUserData(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "targetUserId") int targetUserId){

        if (isNotAdmin(userId)){
            return unauthorized();
        }

        User user = userService.getUserById(targetUserId);

        if (user == null){
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy user", null));
        }

        CustomData<User> data = new CustomData<>(user);
        return ResponseEntity.ok(new CustomResponse<>("Success", "Lấy thành công", data));
    }

    @PostMapping("/add/user")
    @ResponseBody
    public ResponseEntity<?> addUser(
            @RequestParam(name = "userId") int userId,
            @RequestBody UserForm userForm){

        if (isNotAdmin(userId)){
            return unauthorized();
        }

        User user = userService.createUser(userForm);

        if (user == null){
            return ResponseEntity.ok(new CustomResponse<>("Error", "Tạo thất bại. Username hoặc email đã được sử dụng.", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", "Tạo user thành công", null));
    }

    // 7. API Cập nhật User
    @PutMapping("/update/user")
    @ResponseBody
    public ResponseEntity<?> updateUser(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "targetUserId") int targetUserId,
            @RequestBody UserForm userForm){

        if (isNotAdmin(userId)){
            return unauthorized();
        }

        User user = userService.updateUser(targetUserId, userForm);

        if (user == null){
            return ResponseEntity.ok(new CustomResponse<>("Error", "Cập nhật thất bại!", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", "Cập nhật thành công", null));
    }

    // 8. API Xóa User
    @DeleteMapping("/delete/user")
    @ResponseBody
    public ResponseEntity<?> deleteUser(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "targetUserId") int targetUserId){

        if (isNotAdmin(userId)){
            return unauthorized();
        }

        boolean isDeleted = userService.deleteUser(targetUserId);

        if (!isDeleted){
            return ResponseEntity.ok(new CustomResponse<>("Error", "Xóa thất bại!", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", "Xóa thành công", null));
    }

    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getDashboardStats(@RequestParam(name = "userId") int userId){
        if (isNotAdmin(userId)){
            return unauthorized();
        }

        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userRepo.count());
            stats.put("totalMovies", movieRepo.count());
            stats.put("totalComments", commentRepo.count());
            stats.put("totalViews", movieRepo.sumTotalViews());
            return ResponseEntity.ok(new CustomResponse<>("Success", null, new CustomData<>(stats)));
        }
        catch (Exception e){
            return ResponseEntity.ok(new CustomResponse<>("Error", "Lỗi", null));
        }
    }
}