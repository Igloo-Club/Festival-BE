package com.iglooclub.nungil.service;

import com.iglooclub.nungil.domain.*;
import com.iglooclub.nungil.domain.enums.AnimalFace;
import com.iglooclub.nungil.domain.enums.AvailableTime;
import com.iglooclub.nungil.domain.enums.Marker;
import com.iglooclub.nungil.domain.enums.Yoil;
import com.iglooclub.nungil.dto.*;
import com.iglooclub.nungil.exception.ChatRoomErrorResult;
import com.iglooclub.nungil.exception.GeneralException;
import com.iglooclub.nungil.repository.ChatMessageRepository;
import com.iglooclub.nungil.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;

    private final ChatMessageRepository chatMessageRepository;

    /**
     * 채팅 메시지를 데이터베이스에 저장하는 메서드입니다.
     * @param chatDTO 발행된 채팅 메시지 DTO
     * @param member 메시지 발행자 정보
     * @return 기존의 발행된 메시지에서 데이터가 추가된 DTO
     */
    @Transactional
    public ChatDTO save(ChatDTO chatDTO, Member member) {

        ChatRoom chatRoom = getChatRoom(chatDTO.getChatRoomId());

        // 메시지 발행자(member)가 해당 채팅방의 일원이 아니면 예외 발생
        if (!checkChatRoomMember(chatRoom, member)) {
            throw new GeneralException(ChatRoomErrorResult.NOT_MEMBER);
        }

        // 데이터베이스에 채팅 메시지 저장
        ChatMessage chatMessage = ChatMessage.create(chatRoom, member, chatDTO.getContent());
        chatMessageRepository.save(chatMessage);

        return ChatDTO.of(chatDTO.getChatRoomId(), member.getNickname(), chatMessage);
    }

    /**
     * 주어진 채팅방의 메시지 목록을 Slice 형식으로 조회하는 메서드입니다.
     * @param chatRoomId 채팅방 ID
     * @param member 조회를 요청한 회원의 엔티티
     * @param pageRequest 조회되는 페이지 번호, 갯수, 정렬 방식(최근순)
     * @return Slice 형식의 채팅 메시지 목록
     */
    public Slice<ChatMessageListResponse> getMessageSlice(Long chatRoomId, Member member, PageRequest pageRequest) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);

        // 메시지 발행자(member)가 해당 채팅방의 일원이 아니면 예외 발생
        if (!checkChatRoomMember(chatRoom, member)) {
            throw new GeneralException(ChatRoomErrorResult.NOT_MEMBER);
        }

        // 주어진 채팅방의 메시지들을 pageRequest에 맞추어 조회
        Slice<ChatMessage> messageSlice = chatMessageRepository.findByChatRoom(pageRequest, chatRoom);

        // 메시지들을 DTO 리스트로 변환
        List<ChatMessageListResponse> responseList = messageSlice.getContent().stream()
                .map(chatMessage -> {
                    Member sender = chatMessage.getMember();
                    Boolean isSender = member.getId().equals(sender.getId());

                    return ChatMessageListResponse.create(sender, chatMessage, isSender);
                }).collect(Collectors.toList());

        // 변환된 DTO 리스트와 함께 새로운 Slice 객체를 생성하여 반환
        return new SliceImpl<>(responseList, pageRequest, messageSlice.hasNext());
    }

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new GeneralException(ChatRoomErrorResult.CHAT_ROOM_NOT_FOUND));
    }

    /**
     * 사용자가 주어진 채팅방의 일원인지 확인한다.
     * @param chatRoom 채팅방
     * @param member 확인할 사용자
     * @return 채팅방에 속해있는지 여부
     */
    private boolean checkChatRoomMember(ChatRoom chatRoom, Member member) {
        Long memberId = member.getId();
        return memberId.equals(chatRoom.getReceiver().getId()) || memberId.equals(chatRoom.getSender().getId());
    }




    /**
     * 매 시 정각 expireAt이 초과된 채팅룸/채팅 내역을 삭제합니다.
     *
     *
     */
    @Scheduled(cron = "0 0 * * * *") // 매시 정각에 실행
    @Transactional
    public void deleteExpiredChatRoom() {
        LocalDateTime now = LocalDateTime.now();
        List<ChatRoom> chatRoomList = chatRoomRepository.findByExpiredAtBefore(now);
        for (ChatRoom chatRoom : chatRoomList) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    /**
     * 사용자의 채팅방 목록을 Slice 형식으로 조회하는 메서드입니다.
     * @param member 조회를 요청한 회원의 엔티티
     * @param pageRequest 조회되는 페이지 번호, 갯수, 정렬 방식(최근순)
     * @return Slice 형식의 채팅 메시지 목록
     */
    public Slice<ChatRoomListResponse> getChatRoomSlice(Member member,PageRequest pageRequest){
        Slice<ChatRoom> chatRoomSlice = chatRoomRepository.findBySenderOrReceiver(member, member, pageRequest);

        return chatRoomSlice.map(chatRoom -> {
            Member opponent = getOpponent(chatRoom, member);
            AnimalFace animalFace = opponent.getAnimalFace();
            ChatMessage lastMessage = chatRoom.getChatMessageList().isEmpty() ? null : chatRoom.getChatMessageList().get(chatRoom.getChatMessageList().size() - 1);
            String content = lastMessage != null ? lastMessage.getContent() : "지금 연락을 시작하세요!";
            LocalDateTime createdAt = lastMessage != null ? lastMessage.getCreatedAt() : chatRoom.getCreatedAt();
            return new ChatRoomListResponse(
                    (animalFace != null) ? animalFace.getTitle(): null, // 상대방의 AnimalFace
                    opponent.getNickname(), // 상대방 닉네임
                    content, // 마지막 메시지 내용
                    createdAt, // 마지막 메시지 생성 시간
                    chatRoom.getId() // 채팅방 ID
            );
        });
    }
    /**
     * 사용자의 채팅방 목록을 Slice 형식으로 조회하는 메서드입니다.
     * @param member 조회를 요청한 회원의 엔티티
     * @param chatRoomId 채팅방 id
     * @return AvailableTimeAndPlaceResponse 상대방의 가능한 시간과 장소
     */
    public AvailableTimeAndPlaceResponse getAvailableTimeAndPlace(Member member, Long chatRoomId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new GeneralException(ChatRoomErrorResult.CHAT_ROOM_NOT_FOUND));
        Member opponent = chatRoom.getSender().equals(member) ? chatRoom.getReceiver() : chatRoom.getSender();
        List<AvailableTime> timeList = opponent.getAvailableTimeAllocationList().stream()
                .map(AvailableTimeAllocation::getAvailableTime)
                .collect(Collectors.toList());

        List<Marker> markersList = opponent.getMarkerAllocationList().stream()
                .map(MarkerAllocation::getMarker)
                .collect(Collectors.toList());

        List<Yoil> yoilList = opponent.getYoilList();

        return new AvailableTimeAndPlaceResponse(
                yoilList.toString(),
                timeList.toString(),
                markersList.toString()
        );
    }


}
