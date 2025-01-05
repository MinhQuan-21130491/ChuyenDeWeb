package com.chatapp.ChatApp.service.impl;

import com.chatapp.ChatApp.dto.ChatDto;
import com.chatapp.ChatApp.exception.InvalidCredentialsException;
import com.chatapp.ChatApp.exception.NotFoundException;
import com.chatapp.ChatApp.mapper.EntityDtoMapper;
import com.chatapp.ChatApp.modal.Chat;
import com.chatapp.ChatApp.modal.User;
import com.chatapp.ChatApp.modal.UserChat;
import com.chatapp.ChatApp.modal.UserMessage;
import com.chatapp.ChatApp.repository.ChatRepository;
import com.chatapp.ChatApp.request.GroupChatRequest;
import com.chatapp.ChatApp.response.Response;
import com.chatapp.ChatApp.service.iterf.ChatService;
import com.chatapp.ChatApp.service.iterf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final EntityDtoMapper entityDtoMapper;
    @Override
    public Response createChat(User reqUser, Integer userId) {
        User receiUser = userService.findUserById(userId).getUser();
        Chat chatResult = new Chat();
        String message = "Create chat room successfully";
        Chat isChatExist = chatRepository.findSingleChatByUserIds(receiUser, reqUser);
        if(isChatExist != null){
            chatResult = isChatExist;
            message = "Chat room already exists";
        }else {
            //model userchat
            UserChat usc1 = new UserChat();
            usc1.setChat(chatResult);
            usc1.setUser(reqUser);
            //model userchat
            UserChat usc2 = new UserChat();
            usc2.setChat(chatResult);
            usc2.setUser(receiUser);
            Set<UserChat> uss = new HashSet<>();
            uss.add(usc1);
            uss.add(usc2);
            // set model chat
            chatResult.setCreatedBy(reqUser);
            chatResult.setUserChats(uss);
            chatResult.setGroup(false);

            chatRepository.save(chatResult);
        }
        return Response.builder().status(200).chatEntity(chatResult).message(message).build();
    }

    @Override
    public Response findChatById(Integer idChat) {
        Chat chat = chatRepository.findById(idChat)
                .orElseThrow(() -> new NotFoundException("Chat with ID " + idChat + " not found"));
        ChatDto chatDto = entityDtoMapper.mapChatToDtoPlusUserChatDtoAndUserMessageDto(chat);
        return Response.builder().status(200).chat(chatDto).build();
    }
    @Override
    public Response findAllChatByUserId(Integer userId) {
        User user = userService.findUserById(userId).getUser();
        List<Chat> chats = chatRepository.findChatsByUserId(user.getId());
        List<ChatDto> chatDtos = chats.stream().map(entityDtoMapper::mapChatToDtoPlusUserChatDtoAndUserMessageDto).collect(Collectors.toList());
        return Response.builder().status(200).chats(chatDtos).build();
    }
    @Override
    public Response createGroupChat(User reqUser, GroupChatRequest groupChatRequest) {
        Chat groupChat = new Chat();
        groupChat.setCreatedBy(reqUser);
        groupChat.setGroup(true);
        groupChat.setChat_name(groupChatRequest.getChatName());
        groupChat.setChat_image(groupChatRequest.getChat_image());
        UserChat usc = new UserChat();
        usc.setChat(groupChat);
        usc.setUser(reqUser);
        usc.setAdmin(true);
        groupChat.getUserChats().add(usc);
        for(Integer userId: groupChatRequest.getUsersId()) {
            User user = userService.findUserById(userId).getUser();
            UserChat uscInGroup = new UserChat();
            uscInGroup.setChat(groupChat);
            uscInGroup.setUser(user);
            uscInGroup.setAdmin(false);
            groupChat.getUserChats().add(uscInGroup);
        }
        chatRepository.save(groupChat);
        ChatDto groupChatDto = entityDtoMapper.mapChatToDtoPlusUserChatDtoAndUserMessageDto(groupChat);
        return Response.builder().status(200).message("Create group chat successfully").chat(groupChatDto).build();
    }

    @Override
    public Response addUserToGroup(User user, Integer idUserToAdd, Integer idChat) {
        Chat chat = chatRepository.findById(idChat).orElseThrow(() -> new NotFoundException("Not found chat with id " + idChat));
        boolean isAdmin = isAdmin(chat.getUserChats(), user);
        if(isAdmin){
            User userToAdd = userService.findUserById(idUserToAdd).getUser();
            UserChat usc = new UserChat();
            usc.setChat(chat);
            usc.setUser(userToAdd);
            usc.setAdmin(false);
            chat.getUserChats().add(usc);
            chatRepository.save(chat);
            return Response.builder().status(200).message("Add user to group success").build();
        }
        throw new InvalidCredentialsException("You are not admin of this group");
    }

    @Override
    public Response renameGroup(Integer idChat, User user, String newName) {
        Chat chat = chatRepository.findById(idChat).orElseThrow(() -> new NotFoundException("Not found chat with id " + idChat));
        if(isContainUser(chat.getUserChats(), user)) {
            chat.setChat_name(newName);
            chatRepository.save(chat);
            return Response.builder().status(200).message("Rename group successfully").build();
        }
        throw new InvalidCredentialsException("You are not member of this group");
    }

    @Override
    public Response removeUserFromGroup(Integer idUserRemove, Integer idChat, User reqUser) {
        User user = userService.findUserById(idUserRemove).getUser();
        Optional<Chat> optChat = chatRepository.findById(idChat);
        if(optChat.isPresent()){
            Chat chat = optChat.get();
            if(isAdmin(chat.getUserChats(), reqUser)) {
                for (UserChat userChat : chat.getUserChats()) {
                    if(userChat.getUser().equals(user)) {
                        chat.getUserChats().remove(userChat);
                        break;
                    }
                }
                chatRepository.save(chat);
                return Response.builder().status(200).message("Remove user in group successfully").build();
            }
            else if (isContainUser(chat.getUserChats(), reqUser)) {
                if(user.getId().equals(reqUser.getId())) {
                    for (UserChat userChat : chat.getUserChats()) {
                        userChat.getUser().equals(user);
                        chat.getUserChats().remove(userChat);
                        break;
                    }
                    chatRepository.save(chat);
                    return Response.builder().status(200).message("Out group successfully").build();
                }
            }else {
                throw new InvalidCredentialsException("You can't remove user from this group");
            }
        }
        throw new NotFoundException("Not found chat with id " + idChat);
    }

    @Override
    public Response removeChat(Integer idChat, User reqUser) {
        Chat chat = chatRepository.findById(idChat).get();
        for(UserChat userChat: chat.getUserChats()) {
            if(userChat.getUser().equals(reqUser)) {
                userChat.setDeleted(true);
                break;
            }
        }
        for(UserMessage userMessage: chat.getUserMessages()) {
            if(userMessage.getUser().equals(reqUser) && userMessage.getChat().equals(chat)) {
                userMessage.setDeleted(true);
            }
        }
        chatRepository.save(chat);
        return Response.builder().status(200).message("Remove chat successfully").build();
    }
    private boolean isAdmin(Set<UserChat> userChats, User user) {
        for(UserChat userChat: userChats) {
            if(userChat.getUser().equals(user)) {
                if(userChat.isAdmin()) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean isContainUser(Set<UserChat> userChats, User user) {
        for(UserChat userChat: userChats) {
            if(userChat.getUser().equals(user)) {
                   return true;
            }
        }
        return false;
    }
}
