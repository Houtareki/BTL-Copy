package com.example.demo.Controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entity.User;
import com.example.demo.Model.CommonModel.Role; // Import
import com.example.demo.Model.CommonModel.RegisterForm;
import com.example.demo.Model.ResponseModel.CustomData;
import com.example.demo.Model.ResponseModel.CustomResponse;
import com.example.demo.Service.UserService;

import  java.util.List;

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

    // ds user
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(){
        List<User> users = userService.getUsersByRole(Role.USER);
        CustomData<List<User>> data = new CustomData<>(users);
        CustomResponse<List<User>> response = new CustomResponse<>("Success", "Lấy danh sách user thành công", data);
        return ResponseEntity.ok(response);
    }

    //ds admin
    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins(){
        List<User> admins = userService.getUsersByRole(Role.ADMIN);
        CustomData<List<User>> data = new CustomData<>(admins);
        CustomResponse<List<User>> response = new CustomResponse<>("Success", "Lấy danh sách admin thành công", data);
        return ResponseEntity.ok(response);
    }

    // add admin
    @PostMapping("/newad")
    public ResponseEntity<?> addNewUser(@RequestBody RegisterForm registerForm){
        User newAdmin = userService.createAdmin(registerForm);

        if (newAdmin == null){
            return ResponseEntity.ok(
                    new CustomResponse<>("Error", "Tạo admin thất bại. Username hoặc Email đã được sử dụng.", null)
            );
        }

        CustomData<User> data = new CustomData<>(newAdmin);
        CustomResponse<User> response = new CustomResponse<>("Success", "Tạo admin mới thành công!", data);
        return ResponseEntity.ok(response);
    }

    //lock user
    @PutMapping("/users/lock/{id}")
    public ResponseEntity<?> lockUser(@PathVariable("id") int id){
        User user = userService.lockUserAccount(id);

        if (user == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy user", null));
        }

        CustomResponse<User> response = new CustomResponse<>("Success", "Khoá tài khoản thành công", null);
        return ResponseEntity.ok(response);
    }

    //unlock user
    @PutMapping("/users/unlock/{id}")
    public ResponseEntity<?> unlockUser(@PathVariable("id") int id){
        User user = userService.unlockUserAccount(id);

        if (user == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy user", null));
        }

        CustomResponse<User> response = new CustomResponse<>("Success", "Mở khoá tài khoản thành công", null);
        return ResponseEntity.ok(response);
    }

    //promote to ad
    @PutMapping("/users/promote/{id}")
    public ResponseEntity<?> promoteToAdmin(@PathVariable("id") int id){
        User user = userService.changeUserRole(id, Role.ADMIN);

        if (user == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy user", null));
        }
        CustomResponse<User> response = new CustomResponse<>("Success", "Nâng cấp vai trò thành công", null);
        return ResponseEntity.ok(response);
    }

    //demote to user
    @PutMapping("/users/demote/{id}")
    public ResponseEntity<?> demoteToAdmin(@PathVariable("id") int id){
        User user = userService.changeUserRole(id, Role.USER);

        if (user == null) {
            return ResponseEntity.ok(new CustomResponse<>("Error", "Không tìm thấy user", null));
        }
        CustomResponse<User> response = new CustomResponse<>("Success", "Nâng cấp vai trò thành công", null);
        return ResponseEntity.ok(response);
    }
}
