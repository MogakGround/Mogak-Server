package com.example.mogakserver.room.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.mogakserver.room.domain.entity.Room;
import com.example.mogakserver.room.infra.repository.JpaRoomRepository;
import com.example.mogakserver.roomuser.api.request.RoomEnterRequestDTO;
import com.example.mogakserver.roomuser.application.response.RoomEnterResponseDTO;
import com.example.mogakserver.roomuser.application.service.RoomUserService;
import com.example.mogakserver.roomuser.infra.repository.JpaRoomUserRepository;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

@SpringBootTest
@Testcontainers
public class RoomServiceTest {

	@Autowired
	private RoomUserService roomUserService;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private JpaRoomRepository roomRepository;

	@Autowired
	private JpaUserRepository userRepository;

	@Autowired
	private JpaRoomUserRepository roomUserRepository;

	@Container
	private static final GenericContainer<?> redisContainer =
		new GenericContainer<>("redis:latest")
			.withExposedPorts(6379)
			.waitingFor(Wait.forListeningPort())
			.withCreateContainerCmdModifier(cmd ->
				cmd.withHostConfig(new HostConfig().withPortBindings(
					new PortBinding(Ports.Binding.bindPort(63799), new ExposedPort(6379))
				))
			);

	@DynamicPropertySource
	static void configureRedisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", redisContainer::getHost);
		registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
	}

	private Room room;
	private User user;
	private RoomEnterRequestDTO request;

	@BeforeEach
	void setUp() {
		// 테스트 데이터 준비
		room = Room.builder().id(1L).roomName("roomName").isLocked(false).build();
		user = User.builder().id(1L).kakaoId(1234L).version(1).build();
		request = new RoomEnterRequestDTO("password123", false, true);

		roomRepository.save(room);
		userRepository.save(user);
	}

	@Test
	@Transactional
	@DisplayName("방 들어가기 api 테스트")
	public void testEnterRoom_whenValidRequest_thenReturnsRoomUserId() {
		// Given
		Long userId = user.getId();
		Long roomId = room.getId();
		// When
		RoomEnterResponseDTO response = roomUserService.enterRoom(userId, roomId,  request);

		// Then
		assertThat(response).isNotNull();
		assertThat(response.roomUserId()).isEqualTo(1L);
	}

	@Test
	public void testRedisConnection() {
		String key = "testKey";
		String value = "testValue";

		// Redis에 데이터를 저장하고 가져오기
		redisTemplate.opsForValue().set(key, value);
		String result = redisTemplate.opsForValue().get(key);
		assertThat(result).isEqualTo(value);
	}
}

