package ru.diszexuf.streamlive.user;

import org.mapstruct.Mapper;
import ru.diszexuf.streamlive.common.CommonMapper;
import ru.diszexuf.streamlive.user.dto.UserGetRequest;

import java.util.List;

@Mapper(config = CommonMapper.class)
public interface UserMapper {

  UserGetRequest mapToDto(User entity);

  List<UserGetRequest> mapToDtos(List<User> entities);
}
