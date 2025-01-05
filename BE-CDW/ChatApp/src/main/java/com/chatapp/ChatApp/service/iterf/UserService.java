package com.chatapp.ChatApp.service.iterf;

import com.chatapp.ChatApp.modal.User;
import com.chatapp.ChatApp.request.LoginRequest;
import com.chatapp.ChatApp.request.RegistrationRequest;
import com.chatapp.ChatApp.request.UpdateUserRequest;
import com.chatapp.ChatApp.response.Response;


public interface UserService {
    public Response registerUser(RegistrationRequest registrationRequest) ;
    public Response LoginUser(LoginRequest loginRequest);
    public Response findUserById(Integer id) ;
    public Response updateUser(Integer id, UpdateUserRequest req) ;
    public Response searchUser();
    User getLoginUser();

}
