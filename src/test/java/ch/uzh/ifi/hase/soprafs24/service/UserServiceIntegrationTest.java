 package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll();
  }

  @Test
  public void createUser_validInputs_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    // when
    User createdUser = userService.createUser(testUser);

    // then
    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getName(), createdUser.getName());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword"); 
    User createdUser = userService.createUser(testUser);

    // attempt to create second user with same username
    User testUser2 = new User();

    // change the name but forget about the username
    testUser2.setName("testName2");
    testUser2.setUsername("testUsername");

    // check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
  }

  @Test
  public void getUserById_validId_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    User createdUser = userService.createUser(testUser);

    // when
    User foundUser = userService.getUserById(createdUser.getId());

    // then
    assertEquals(createdUser.getId(), foundUser.getId());
    assertEquals(createdUser.getName(), foundUser.getName());
    assertEquals(createdUser.getUsername(), foundUser.getUsername());
    assertEquals(createdUser.getStatus(), foundUser.getStatus());
  }

  @Test
  public void getUserById_invalidId_throwsException() {
    // given/when/then
    assertThrows(ResponseStatusException.class, () -> userService.getUserById(9999L));
  }

  @Test
  public void loginUser_validCredentials_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    User createdUser = userService.createUser(testUser);
    createdUser.setStatus(UserStatus.OFFLINE);
    userRepository.save(createdUser);
    userRepository.flush();

    User loginUser = new User();
    loginUser.setUsername("testUsername");
    loginUser.setPassword("testPassword");

    // when
    User loggedInUser = userService.loginUser(loginUser);

    // then
    assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
    assertEquals(createdUser.getId(), loggedInUser.getId());
  }

  @Test
  public void updateUser_validInput_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    User createdUser = userService.createUser(testUser);

    User updateUser = new User();
    updateUser.setUsername("newUsername");
    Date birthday = new Date();
    updateUser.setBirthday(birthday);

    // when
    userService.updateUser(createdUser.getId(), updateUser, createdUser.getToken());


    // then
    User updatedUser = userService.getUserById(createdUser.getId());
    assertEquals("newUsername", updatedUser.getUsername());
    assertEquals(birthday, updatedUser.getBirthday());
  }
}
