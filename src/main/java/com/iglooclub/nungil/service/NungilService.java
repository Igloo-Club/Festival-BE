package com.iglooclub.nungil.service;

import com.iglooclub.nungil.domain.*;
import com.iglooclub.nungil.domain.enums.*;
import com.iglooclub.nungil.domain.events.NungilMatchedEvent;
import com.iglooclub.nungil.domain.events.NungilSentEvent;
import com.iglooclub.nungil.dto.*;
import com.iglooclub.nungil.exception.ChatRoomErrorResult;
import com.iglooclub.nungil.exception.GeneralException;
import com.iglooclub.nungil.exception.MemberErrorResult;
import com.iglooclub.nungil.exception.NungilErrorResult;
import com.iglooclub.nungil.repository.ChatRoomRepository;
import com.iglooclub.nungil.repository.MemberRepository;
import com.iglooclub.nungil.repository.NungilRepository;
import com.iglooclub.nungil.util.CoolSMS;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class NungilService {
    private final MemberRepository memberRepository;
    private final NungilRepository nungilRepository;

    private final ChatRoomRepository chatRoomRepository;

    private final MemberService memberService;

    private final CoolSMS coolSMS;

    private final ApplicationEventPublisher publisher;

    private static final String BASE_URL = "https://nungil.com";

    private static final Long RECOMMENDATION_LIMIT = 3L;

    /* 눈길 관리 */
    /**
     * 추천 눈길을 생성하는 api 입니다
     *
     * @request member
     * @return nungilResponse 추천되는 사용자 눈길 정보
     */
    @Transactional
    public NungilResponse addRecommendMember(Member member){

        Nungil newNungil = Nungil.create(member, member, NungilStatus.RECOMMENDED);
        nungilRepository.save(newNungil);

        return convertToNungilResponse(newNungil);
    }

    /**
     * 요청 눈길상태의 프로필을 전체 조회하는 api입니다
     *
     * @param  page 페이지 정보
     * @param size 페이지 정보
     * @param status 요청 눈길 상태
     *
     * @return NungilPageResponse 슬라이스 정보 반환
     */
    public Slice<NungilSliceResponse> getNungilSliceByMemberAndStatus(Member member, NungilStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size,Sort.by("createdAt").descending());

        // Nungil 엔티티를 데이터베이스에서 조회
        Slice<Nungil> nungilSlice = nungilRepository.findAllByMemberAndStatus(pageRequest, member, status);

        // Nungil 엔티티를 NungilPageResponse DTO로 변환
        List<NungilSliceResponse> nungilResponses = nungilSlice.getContent().stream()
                .map(nungil -> NungilSliceResponse.create(nungil, nungil.getReceiver()))
                .collect(Collectors.toList());



        // 변환된 DTO 리스트와 함께 새로운 Slice 객체를 생성하여 반환
        return new SliceImpl<>(nungilResponses, pageRequest, nungilSlice.hasNext());
    }

    /**
     * 추천 눈길상태의 프로필을 전체 조회하는 api입니다. 오늘 프로필을 추천받지 않았다면, 하나 뽑습니다.
     *
     * @param  page 페이지 정보
     * @param size 페이지 정보
     *
     * @return NungilPageResponse 슬라이스 정보 반환
     */
    @Transactional
    public Slice<NungilSliceResponse> getRecommendedNungilSlice(Member member, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size,Sort.by("createdAt").descending());

        // Nungil 엔티티를 데이터베이스에서 조회
        Slice<Nungil> nungilSlice = nungilRepository.findAllByStatus(pageRequest, NungilStatus.RECOMMENDED);

        // Nungil 엔티티를 NungilPageResponse DTO로 변환
        List<NungilSliceResponse> nungilResponses = nungilSlice.getContent().stream()
                .filter(nungil -> !member.equals(nungil.getReceiver()))
                .map(nungil -> NungilSliceResponse.create(nungil, nungil.getReceiver()))
                .collect(Collectors.toList());



        // 변환된 DTO 리스트와 함께 새로운 Slice 객체를 생성하여 반환
        return new SliceImpl<>(nungilResponses, pageRequest, nungilSlice.hasNext());
    }

    /**
     * 특정 눈길 정보를 조회하는 api입니다
     *
     * @param nungilId 눈길 id
     * @return nungilResponse 특정 눈길 정보
     */
    public NungilResponse getNungilDetail(Long nungilId){
        Nungil nungil = nungilRepository.findById(nungilId)
                .orElseThrow(() -> new GeneralException(NungilErrorResult.NUNGIL_NOT_FOUND));
        Member member = nungil.getReceiver();
        NungilResponse nungilResponse = convertToNungilResponse(nungil);
        return nungilResponse;
    }

    /**
     * 눈길을 보내는 api입니다
     * member에게 recommend status의 눈길을 SENT로 수정하며
     * receiver에게 status가 RECEIVED인 눈길을 생성합니다
     *
     * @param nungilId 눈길 id
     */
    @Transactional
    public void sendNungil(Member member, Long nungilId){
        Nungil nungil = nungilRepository.findById(nungilId)
                .orElseThrow(()->new GeneralException(NungilErrorResult.NUNGIL_NOT_FOUND));
        Member receiver = nungil.getReceiver();

        if(!nungil.getStatus().equals(NungilStatus.RECOMMENDED)){
            throw new GeneralException(NungilErrorResult.NUNGIL_WRONG_STATUS);
        }

        //이미 눈길을 보냈을 시 중단
        List<Nungil> receiverNungilList = nungilRepository.findAllByMemberAndReceiverAndStatus(receiver, member, NungilStatus.RECEIVED);
        if(receiverNungilList.size() > 0){
            return ;
        }

        //사용자의 눈길 상태를 SENT, 만료일을 일주일 뒤로 설정
        Nungil newNungil1 = Nungil.create(member, receiver, NungilStatus.SENT);
        nungilRepository.save(newNungil1);


        //눈길 받는 사용자 눈길 객체 생성 및 저장
        Nungil newNungil2 = Nungil.create(receiver, member, NungilStatus.RECEIVED);
        nungilRepository.save(newNungil2);

        // 눈길 받은 사용자에게 알림 전송
//        String phoneNumber = receiver.getPhoneNumber();
//        String url = BASE_URL + "/receiveddetailpage/" + newNungil.getId();
//        String text = "[눈길] 새로운 눈길이 도착했어요. 얼른 확인해보세요!\n" + url;
//
//        coolSMS.send(phoneNumber, text);
//        this.sendNungilSMS(receiver, newNungil2);
    }
    public void sendNungilSMS(Member sender, Nungil sentNungil){
        publisher.publishEvent(new NungilSentEvent(sender, sentNungil));
    }

    /**
     * 눈길을 매칭하는 api입니다
     *
     *
     * @param nungilId 눈길 id
     */
    @Transactional
    public void matchNungil(Long nungilId){
        Nungil receivedNungil = nungilRepository.findById(nungilId)
                .orElseThrow(()->new GeneralException(NungilErrorResult.NUNGIL_NOT_FOUND));
        //눈길이 잘못된 상태일 시 에러 발생
        if(!receivedNungil.getStatus().equals(NungilStatus.RECEIVED)){
            throw new GeneralException(NungilErrorResult.NUNGIL_WRONG_STATUS);
        }

        //사용자의 눈길을 MATCHED 상태로 변경
        receivedNungil.setStatus(NungilStatus.MATCHED);
        receivedNungil.setExpiredAtNull();

        //수취자의 눈길 조회 후 MATCHED 상태로 변경
        Member member = receivedNungil.getMember();
        Member sender = receivedNungil.getReceiver();
        Optional<Nungil> optionalNungil = nungilRepository.findFirstByMemberAndReceiver(sender, member);
        if(optionalNungil.isEmpty()){
            throw new GeneralException(NungilErrorResult.NUNGIL_NOT_FOUND);
        }
        Nungil sentNungil = optionalNungil.get();
        sentNungil.setStatus(NungilStatus.MATCHED);
        sentNungil.setExpiredAtNull();


        // 매칭된 사용자 간에 겹치는 시간, 마커를 조회하여 저장
        List<Marker> marker = findCommonMarkers(member, sender);
        AvailableTime time = null;

        List<AvailableTime> commonAvailableTimes = findCommonAvailableTimes(member, sender);
        if(!commonAvailableTimes.isEmpty()){
            time = commonAvailableTimes.get(0);
        }
        receivedNungil.update(marker, time);
        sentNungil.update(marker, time);

        // 매칭된 사용자들을 채팅방에 초대
        ChatRoom chatRoom = ChatRoom.create(member, sender);
        chatRoomRepository.save(chatRoom);

        // 눈길 보낸 사용자에게 알림 전송
//        String phoneNumber = sender.getPhoneNumber();
//        String url = BASE_URL + "/finishmatch/" + sentNungil.getId();
//        String text = "[눈길] 축하해요! 서로의 눈길이 닿았어요. 채팅방을 통해 두 분의 첫만남 약속을 잡아보세요.\n" + url;


        this.sendMatchSMS(sender, sentNungil);
//        coolSMS.send(phoneNumber, text);
    }

    public void sendMatchSMS(Member sender, Nungil sentNungil){
        publisher.publishEvent(new NungilMatchedEvent(sender, sentNungil));
    }


    /**
     * 공통 매칭 정보를 조회하는 api입니다
     * @param nungilId 눈길 id
     * @response nungilMatchResponse 눈길 매칭 정보
     */
    public NungilMatchResponse getMatchedNungil(Long nungilId, Member member){

        Nungil nungil = nungilRepository.findById(nungilId)
                .orElseThrow(() -> new GeneralException(NungilErrorResult.NUNGIL_NOT_FOUND));

        // 요청을 보낸 사용자가 주어진 눈길의 소유자인지 확인
        if (!nungil.getMember().equals(member)) {
            throw new GeneralException(MemberErrorResult.NOT_OWNER);
        }

        List<ChatRoom> chatRooms = chatRoomRepository.findByMembers(nungil.getMember(), nungil.getReceiver());

        // chat room 2개 이상일 경우 예외 처리
        if (chatRooms.size() > 1) {
            throw new GeneralException(ChatRoomErrorResult.CHAT_ROOM_MORE_THAN_ONE);
        }

        // 두 사용자가 속한 채팅방이 존재하지 않는 경우 null을 반환
        Long chatRoomId = chatRooms.isEmpty() ? null : chatRooms.get(0).getId();


        return NungilMatchResponse.create(nungil, chatRoomId);
    }



    private Member getMember(Principal principal) {
        return memberService.findById(Long.parseLong(principal.getName()));
    }

    //Member 엔티티의 데이터를 NungilResponseDTO로 변환하는 메서드
    private NungilResponse convertToNungilResponse(Nungil nungil) {
        return NungilResponse.builder()
                .id(nungil.getReceiver().getId())
                .sex(nungil.getReceiver().getSex())
                .age(LocalDateTime.now().minusYears(nungil.getReceiver().getBirthdate().getYear()).getYear())
                .nickname(nungil.getReceiver().getNickname())
                .animalFace(nungil.getReceiver().getAnimalFace().getTitle())
                .mbti(nungil.getReceiver().getMbti())
                .job(nungil.getReceiver().getJob())
                .faceDepictionAllocationList(nungil.getReceiver().getFaceDepictionAllocationsAsString())
                .personalityDepictionAllocationList(nungil.getReceiver().getPersonalityDepictionAllocationAsString())
                .description(nungil.getReceiver().getDescription())
                .hobbyAllocationList(nungil.getReceiver().getHobbyAllocationAsString())
                .expiredAt(nungil.getExpiredAt())
                .build();
    }

    //두 사용자의 공통 시간을 추출
    public List<AvailableTime> findCommonAvailableTimes(Member member1, Member member2) {
        List<AvailableTimeAllocation> list1 = member1.getAvailableTimeAllocationList();
        List<AvailableTimeAllocation> list2 = member2.getAvailableTimeAllocationList();

        Set<AvailableTime> timesSet1 = list1.stream()
                .map(AvailableTimeAllocation::getAvailableTime)
                .collect(Collectors.toSet());

        Set<AvailableTime> timesSet2 = list2.stream()
                .map(AvailableTimeAllocation::getAvailableTime)
                .collect(Collectors.toSet());

        // 교집합 찾기
        timesSet1.retainAll(timesSet2);

        return new ArrayList<>(timesSet1);
    }
    //두 사용자의 공통 마커를 추출
    public List<Marker> findCommonMarkers(Member member1, Member member2) {
        Set<Marker> markerSet1 = member1.getMarkerAllocationList().stream()
                .map(MarkerAllocation::getMarker)
                .collect(Collectors.toSet());

        Set<Marker> markerSet2 = member2.getMarkerAllocationList().stream()
                .map(MarkerAllocation::getMarker)
                .collect(Collectors.toSet());

        // 교집합 찾기
        markerSet1.retainAll(markerSet2);

        return new ArrayList<>(markerSet1);
    }

}