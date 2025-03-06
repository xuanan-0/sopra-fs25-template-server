package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User testUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser = new User();
    testUser.setId(1L);
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword"); 
    testUser.setCreationDate(new Date());
    testUser.setToken("test-token");

    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
  }

  @Test
  public void createUser_validInputs_success() {
    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    User createdUser = userService.createUser(testUser);

    // then
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getName(), createdUser.getName());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  //@Test
   //public void createUser_duplicateName_throwsException() {
   // given -> a first user has already been created
   //User existingUser = new User();
   //existingUser.setId(2L);
   //existingUser.setName("testName");
   //existingUser.setUsername("existingUsername");
   //existingUser.setPassword("testPassword");
   //existingUser.setCreationDate(new Date());

   //Mockito.when(userRepository.findByName("testName")).thenReturn(existingUser);
   //Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(null);

   //User newUser = new User();
   //newUser.setName("testName"); 
   //newUser.setUsername("newUsername"); 
   //newUser.setPassword("testPassword");
  
   // then -> attempt to create second user with same name -> check that an error is thrown
   //assertThrows(ResponseStatusException.class, () -> userService.createUser(newUser));
  
  //Mockito.verify(userRepository, Mockito.times(1)).findByName(Mockito.anyString());
  //}

  @Test
  public void createUser_duplicateInputs_throwsException() {
    // given -> a first user has already been created
    testUser.setName("testName");
    testUser.setUsername("testUsername");
    userService.createUser(testUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByName(Mockito.any())).thenReturn(testUser);
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

    User testUser2 = new User();
    testUser2.setName("testName");
    testUser2.setUsername("testUsername");
    testUser2.setPassword("testPassword");

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
  }

  @Test
  public void getUserById_validId_returnsUser() {
    // given
    Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));

    // when
    User foundUser = userService.getUserById(1L);

    // then
    assertEquals(testUser.getId(), foundUser.getId());
    assertEquals(testUser.getName(), foundUser.getName());
    assertEquals(testUser.getUsername(), foundUser.getUsername());
  }

  @Test
  public void getUserById_invalidId_throwsException() {
    // given
    Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(java.util.Optional.empty());

    // when/then
    assertThrows(ResponseStatusException.class, () -> userService.getUserById(1L));
  }

  @Test
  public void loginUser_validCredentials_success() {
    // given
    testUser.setPassword("correctPassword");
    testUser.setStatus(UserStatus.OFFLINE);

    User loginUser = new User();
    loginUser.setUsername("testUsername");
    loginUser.setPassword("correctPassword");

    Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);
    Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(testUser);

    // when
    User loggedInUser = userService.loginUser(loginUser);

    // then
    assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    Mockito.verify(userRepository, Mockito.times(1)).flush();
  }

  @Test
  public void loginUser_wrongPassword_throwsException() {
    // given
    testUser.setPassword("correctPassword");

    User loginUser = new User();
    loginUser.setUsername("testUsername");
    loginUser.setPassword("wrongPassword");

    Mockito.when(userRepository.findByUsername(testUser.getUsername())).thenReturn(testUser);

    // when/then
    assertThrows(ResponseStatusException.class, () -> userService.loginUser(loginUser));
  }

  @Test
  public void updateUser_validInput_success() {
    // given
    testUser.setUsername("oldUsername");

    User updateUser = new User();
    updateUser.setUsername("newUsername");
    updateUser.setBirthday(new Date());

    Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser)); 
    Mockito.when(userRepository.findByUsername("newUsername")).thenReturn(null);
    Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(testUser);
    
    // when
    userService.updateUser(testUser.getId(), updateUser, testUser.getToken());

    // then
    assertEquals("newUsername", testUser.getUsername());
    assertNotNull(testUser.getBirthday());
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    Mockito.verify(userRepository, Mockito.times(1)).flush();
  }

}
