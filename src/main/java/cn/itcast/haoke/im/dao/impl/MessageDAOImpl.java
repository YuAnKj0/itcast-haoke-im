package cn.itcast.haoke.im.dao.impl;

import cn.itcast.haoke.im.dao.MessageDAO;
import cn.itcast.haoke.im.pojo.Message;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MessageDAOImpl implements MessageDAO {
    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 查询点对点聊天记录
     *
     * @param fromId
     * @param toId
     * @param page
     * @param rows
     * @return
     */
    @Override
    public List<Message> findListByFromAndTo(Long fromId, Long toId, Integer page, Integer rows) {
        //用户A发送给用户B的条件
        Criteria criteriaFrom=new Criteria().andOperator(
                Criteria.where("from.id").is(fromId),
                Criteria.where("to.id").is(toId)
        );
        //用户B发送给用户A的条件
        Criteria criteriaTo=new Criteria().andOperator(
                Criteria.where("from.id").is(toId),
                Criteria.where("to.id").is(fromId)
        );
        Criteria criteria=new Criteria().orOperator(criteriaFrom,criteriaTo);
        PageRequest pageRequest=PageRequest.of(page-1,rows, Sort.by(Sort.Direction.ASC,"sendDate"));
        Query query=Query.query(criteria).with(pageRequest);
        return this.mongoTemplate.find(query,Message.class);
    }
    
    /**
     * 根据id查询数据
     *
     * @param id
     * @return
     */
    @Override
    public Message findMessageById(String id) {
        return this.mongoTemplate.findById(new ObjectId(id), Message.class);
    }
    
    /**
     * 更新消息状态
     *
     * @param id
     * @param status
     * @return
     */
    @Override
    public UpdateResult updateMessageState(ObjectId id, Integer status) {
        Query query = Query.query(Criteria.where("id").is(id));
        Update update = Update.update("status", status);
        if (status.intValue() == 1) {
            update.set("send_date", new Date());
        } else if (status.intValue() == 2) {
            update.set("read_date", new Date());
        }
        return this.mongoTemplate.updateFirst(query, update, Message.class);
    }
    
    /**
     * 新增消息数据
     * *
     * * @param message
     * * @return
     *
     * @param message
     */
    @Override
    public Message saveMessage(Message message) {
        message.setId(ObjectId.get());
        message.setSendDate(new Date());
        message.setStatus(1);
        return this.mongoTemplate.save(message);
    }
    
    /**
     * * 根据消息id删除数据
     * *
     * * @param id
     * * @return
     *
     * @param id
     */
    @Override
    public DeleteResult deleteMessage(String id) {
        Query query = Query.query(Criteria.where("id").is(id));
        return this.mongoTemplate.remove(query, Message.class);
    }
}
