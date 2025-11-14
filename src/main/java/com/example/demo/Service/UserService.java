package com.example.demo.Service;

import com.example.demo.Model.CommonModel.UserForm;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.User;
import com.example.demo.Model.CommonModel.LoginForm;
import com.example.demo.Model.CommonModel.RegisterForm;
import com.example.demo.Model.CommonModel.Role;
import java.util.List;

@Service
public interface UserService {
    public User comfirmUserLogin(LoginForm loginForm);
    public User checkRegisterForm(RegisterForm registerForm);
    public User findByUserId(int userId);
    public User findByEmail(String email);
    public User activateUser(int userId);
    public User changeUserPassword(int userId, String newPassword, String comfirmPassword);
    public User updateUserFavoritedRepo(int userId, int movieId);
    public User updateUserSavedRepo(int userId, int movieId);

    public List<User> getUsersByRole(Role role);
    public User lockUserAccount(int userId);
    public User unlockUserAccount(int userId);
    public User changeUserRole(int userId, Role newRole);
    public User createAdmin(RegisterForm registerForm);

    public User getUserById(int userId);
    public Page<User> getAllUsers(int pageNo);
    public Page<User> searchUsers(String keyword, int pageNo);
    public User createUser(UserForm userForm);
    public User updateUser(int userId, UserForm userForm);
    public boolean deleteUser(int userId);
}
