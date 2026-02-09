package com.example.study.imserver.handler;


import com.example.study.imserver.model.ChatMessage;
import com.example.study.imserver.model.MessageType;
import com.example.study.imserver.service.ChatRoomService;
import com.example.study.imserver.util.ChannelManager;
import com.example.study.imserver.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Netty服务端业务处理器（处理客户端消息）
 * @author 编程导师
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ImServerHandler extends SimpleChannelInboundHandler<String> {

    @Autowired
    private ChannelManager channelManager;

    @Autowired
    private ChatRoomService chatRoomService;

    /**
     * 客户端连接成功时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端连接成功：{}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    /**
     * 处理客户端发送的消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("收到客户端消息：{}", msg);
        try {
            // 反序列化为消息对象
            ChatMessage chatMessage = JsonUtil.fromJson(msg, ChatMessage.class);
            if (chatMessage == null) {
                log.error("消息格式错误：{}", msg);
                return;
            }

            channelManager.addChannel(chatMessage.getSenderId(), ctx.channel());

            // 根据消息类型处理
            MessageType msgType = MessageType.getByCode(chatMessage.getMsgType());
            switch (msgType) {
                case JOIN_ROOM:
                    // 加入聊天室：先绑定用户ID与Channel，再执行加入逻辑
                    chatRoomService.joinChatRoom(chatMessage.getRoomId(), chatMessage.getSenderId());
                    break;
                case QUIT_ROOM:
                    // 退出聊天室
                    chatRoomService.quitChatRoom(chatMessage.getRoomId(), chatMessage.getSenderId());
                    break;
                case CHAT_MESSAGE:
                    // 发送普通消息
                    chatRoomService.sendMessage(chatMessage);
                    break;
                case SYSTEM_NOTICE:
                    log.info("收到系统通知：{}", chatMessage.getContent());
                    break;
                default:
                    log.warn("不支持的消息类型：{}", msgType);
            }
        } catch (Exception e) {
            log.error("处理客户端消息失败", e);
        }
    }

    /**
     * 客户端断开连接时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端断开连接：{}", ctx.channel().remoteAddress());
        // 移除Channel映射
        channelManager.removeChannel(ctx.channel());
        super.channelInactive(ctx);
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端通信异常", cause);
        ctx.close(); // 关闭通道
    }
}