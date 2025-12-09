package com.dev_high.common.context;

public class UserContext {

  private static final ThreadLocal<UserInfo> context = new ThreadLocal<>();

  public record UserInfo(String userId, String role) {

  }

  
  public static void set(UserInfo userInfo) {
    context.set(userInfo);
  }

  public static UserInfo get() {
    return context.get();
  }

  public static void clear() {
    context.remove();
  }

}
