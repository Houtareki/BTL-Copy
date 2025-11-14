package com.example.demo.Controller;

import com.example.demo.Model.CommonModel.UserForm;
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

@Controller
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserService userService;

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

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<?> getUsers(
            @RequestParam(name = "userId") int userId,
            @RequestParam(name = "pageNo") int pageNo) {

        if (isNotAdmin(userId)) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Bạn không có quyền truy cập", null));
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
            return ResponseEntity.ok(new CustomResponse<>("Error", "Bạn không có quyền truy cập", null));
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
            return ResponseEntity.ok(new CustomResponse<>("Error", "Bạn không có quyền truy cập", null));
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
            return ResponseEntity.ok(new CustomResponse<>("Error", "Bạn không có quyền truy cập", null));
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
            return ResponseEntity.ok(new CustomResponse<>("Error", "Bạn không có quyền truy cập", null));
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
            return ResponseEntity.ok(new CustomResponse<>("Error", "Bạn không có quyền truy cập", null));
        }

        boolean isDeleted = userService.deleteUser(targetUserId);

        if (!isDeleted){
            return ResponseEntity.ok(new CustomResponse<>("Error", "Xóa thất bại!", null));
        }

        return ResponseEntity.ok(new CustomResponse<>("Success", "Xóa thành công", null));
    }
}