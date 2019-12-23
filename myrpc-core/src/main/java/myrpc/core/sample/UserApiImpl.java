package myrpc.core.sample;

import java.util.Random;

public class UserApiImpl implements UserApi {
    @Override
    public User login(String username, String password) {
        User user = new User();
        user.setPhone("13888888888");
        user.setUid(new Random().nextInt() + "");
        user.setUsername(username);

        System.out.println("write response: " + user);

        return user;
    }
}
