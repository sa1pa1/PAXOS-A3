# **Pham Phuong Ngan Bui (a1867987) PAXOS**
This is a Paxos implementation that utilises three classes 
> Acceptor.java  
> Proposer.java  
> Paxosmember.java  

**The criterias:** 
- [x] Paxos implementation works when two councillors send voting proposals at the same time
- [x] Paxos implementation works in the case where all M1-M9 have immediate responses to voting queries
- [x] Paxos implementation works when M1 – M9 have responses to voting queries suggested by several profiles  
      (immediate response, small delay, large delay and no response)
- [x] Paxos implementation works with suggested profiles and when either M2 or M3 goes offline.
_______
## Test 1: Implementations work for immediate responses  
> [!NOTE]  
> Acceptor.java, Proposer.java and Paxosmember.java assumes no delay to members. Providing immediate responses.

> ### Test 1.a: One proposer  
> - Redirect to the src, in terminal run: `cd src`  
> - compile all files, run `javac *.java`  
> - To compile the first test of immediate responses with one proposer, run:  
> -      java ImmediateResponseTest1  
> - In this test, M1 will win/ reach consensus as its the only proposer. 

> ### Test 1.b: Concurrent: Two proposers  
> - To compile the testing of two concurrent proposers with immediate response run:  
> -      java ImmediateResponseTest2  
> - In this test, M2 should win as it has highest proposal ID 

> ### Test 1.c: Concurrent: Three proposers (extra test)
> - To compile the testing of three concurrent proposers with immediate response run:  
> -      java ImmediateResponseTest3  
> - In this test, M3 should win as it has the highest proposal ID


**Criteria reached:**
- [x] Paxos implementation works when two councillors send voting proposals at the same time
- [x] Paxos implementation works in the case where all M1-M9 have immediate responses to voting queries
_______
## Test 2: Members with suggested delay profiles
> [!NOTE]  
> Acceptor.java, Proposer.java and Paxosmember.java assumes no delay to members. Providing immediate responses.
> The delay is then integrated into the testing by calling the class `DelayBehaviour.java` on members. Delay profiles are as follow:  

>> M1 respond immediately, no delay.
>
>> M2 sometimes (< 30% of time) is at cafe, and 70% is at home where connection is largely delayed. This is  simulated using `SimulateLargeDelay(String memberId)`, where it delays 10 - 15 seconds.
>
>> M3 is not as slow as M2, but not as fast as M1. This is simulated using `simulateSmallDelay(String memberId)`
>> where it delays messages between 2 - 5 seconds.  
>> Sometimes, M3 is camping (< 30% of time), the message is dropped and doesnt respond at all. 
>
>> M4 - M9 have busy schedule, so their delay is simulated using `simulateBusySchedule(String memberId)`, delaying between 3-6 seconds.

> ### Test 2.a: One proposer
> - This test applies the suggested delay profiles to all member. Then have M1 proposes to visualise how the delay profiles behave.  
> - To test this run:
> -       java MemberDelayTest1  
> - In this test, M1 is the only proposer and will reach consensus. However, it will be able to handle delays.

> ### Test 2.b: Concurrent: Two proposers (M1 and M2)
> - In this test, M1 and M2 are proposers. This test not only studies the behaviour of paxos protocol favouring higher proposal ID but also the effects of delays on which member will win.
> - M2 has a higher proposal ID than M1 in this test. However, due to its delay M1 can win. 
> - To test this, run:
> -       java MemberDelayTest2
>  **(Run and see which scenario you get)**

> ### Test 2.c: Concurrent: Two proposers (M2 and M3) 
> - In this test, M2 and M3 are proposers. This test not only studies the behaviour of paxos protocol favouring higher proposal ID but also the effects of delays on which member will win.
> - M2 has a higher proposal ID than M3 in this test.  
> -  CASE 1: Due to M2's delay, M3 has a chance at winning despite M3 having small delays.  
> -  CASE 2: If M2 is at cafe or M3 is not responding, M2 has a chance at winning.
> -  HOWEVER, Randomised delays in the simulation mean that the "expected" outcomes do not always occur. For example, M2 may occasionally overcome large delays if acceptors respond favorably or M3's delays accumulate due to network conditions. Both M2 and M3 have valid chances of winning depending on how delays, acceptor responses, and timing play out.  
> -  To test this, run:
> -       java MemberDelayTest3   
>  **(Run and see which scenario you get)**

**Criteria reached:**
- [x] Paxos implementation works when two councillors send voting proposals at the same time
- [x] Paxos implementation works when M1 – M9 have responses to voting queries suggested by several profiles  
      (immediate response, small delay, large delay and no response)
_______

## Test 3: Failover when a proposer goes offline

>### Test 3.a: Two concurrent proposers, one shuts down
> - In this test, we have two proposers, M1 and M2. Where M2 has the higher proposalID.
> - This test simulate that when M2 proposes then shuts down. Acceptors no longer able to make further acceptances as M1 has a lower proposal ID.
> - M1 then retry with a higher proposalID. To test this run:
> -       java ShutdownTest1  
>   **EXPECTED OUTPUT: M1 should win.**

>### Test 3.b: Three concurrent proposers, one shuts down (extension test) 
> - In this test, we have 3 proposers, M1, M2 and M3. M3 has the highest proposalID.
> - When M3 shuts down, M1 and M2 will propose new proposal ID.
> > If M1 retries later than M2, M1 wins  
> > If M2 retries later than M1, M2 wins
> > 
> Notably, it is possible when M1 retries first and has lower proposalID than M2.
> Due to the known suggest delay profile, M2 connections may delay causing M1 to win. M2 will not always win in cases where it has higher proposalID. In Paxos, a lower proposal ID can win if it receives the majority of promises and successfully proceeds through the accept phase before competing proposals with higher IDs complete the process.
>>
> - To test this, run:  
> -       java ShutdownTest2   

>### Test 3.c: Three concurrent proposers, two shuts down (extension test)
> - In this test, we have 3 proposers, M1, M2 and M3.
> - M2 and M3 shuts down concurrently.
> - M1 will retry with higher proposalID and win.
> - To test this, run:
> -       java ShutdownTest3
> - **EXPECTED OUTPUT: M1 should win.** 


>### Test 3.d: Three concurrent proposers, varying shutdown (extension test)
> - In this test, we have 3 proposers, M1, M2 and M3.
> - M3 shuts down first, then 3 seconds later M2 shuts down. This is to show the behaviour of failover when they shut down at different times.
> - To test this, run:
> -       java ShutdownTest4
> -  **EXPECTED OUTPUT: M1 should win.** 

**Criteria reached:**
- [x] Paxos implementation works with suggested profiles and when either M2 or M3 goes offline.
- [x] Paxos implementation works when two councillors send voting proposals at the same time


# END OF TESTING. THANK YOU :))
## A PDF is provided to show the outputs of each test. "TEST-OUTPUTS".  

