package priv.ljh.uniapp.mapper;

import io.swagger.annotations.ApiModel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import priv.ljh.uniapp.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lijinghai
 * @since 2021-05-28
 */

@Mapper
@Repository
@ApiModel("用户信息接口类")
public interface UserMapper extends BaseMapper<User> {

    /**
     * 自定义查询方法
     * @param user
     * @return
     */
    @Select("select * from user where account = #{account} and password= #{password}")
    User login(User user);

    /**
     * 判断用户名是否重复
     * @param account
     * @return
     */
//    @Select("select count(*) from user where account = #{account};")
//    int selectCount(@Param("account") String account);

    @Insert("insert into user (account,mobile,password) select #{account},#{account},#{password} from user where not exists(select id from user where account = #{account}) limit 0,1")
    int insertUser(@Param("account") String account,@Param("password") String password);


    /**
     * 查询添加数据的id
     * @param account
     * @return
     */
    @Select("select id from user where account = #{account}")
    int selectId(@Param("account") String account);
}
