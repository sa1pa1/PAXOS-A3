# **Pham Phuong Ngan Bui (1867987) PAXOS**
This is a Paxos implementation that utilises three classes 
> Acceptor.java  
> Proposer.java  
> Paxosmember.java  

**The criterias:** 
- [x] Paxos implementation works when two councillors send voting proposals at the same time
- [x] Paxos implementation works in the case where all M1-M9 have immediate responses to voting queries
- [x] Paxos implementation works when M1 – M9 have responses to voting queries suggested by several profiles  
      (immediate response, small delay, large delay and no response)
- [x] Paxos implementation works with suggested profiles and when either M2 or M2 goes offline.

## Test 1: Implementations works for immediate responses  
> [!NOTE]  
> Acceptor.java, Proposer.java and Paxosmember.java assumes no delay to members. Providing immediate responses.

> ### Test 1.a: One proposer  
> - Redirect to the src, in terminal run: `cd src`  
> - compile all files, run `javac *.java`  
> - To compile the first test of immediate responses with one proposer, run:  
> `java ImmediateResponseTest1`

> ### Test 1.b: Concurrent responses: Two proposers  
> - To compile the testing of two concurrent proposers with immediate response run:  
> `java ImmediateResponseTest2`  
> In this test, M2 should win as it has highest proposal ID 

> ### Test 1.c: Concurrent responses: Three proposers (extra test)
> - To compile the testing of three concurrent proposers with immediate response run:  
> `java ImmediateResponseTest3`  
> In this test, M3 should win as it has the highest proposal ID

- [x] Paxos implementation works when two councillors send voting proposals at the same time
- [x] Paxos implementation works in the case where all M1-M9 have immediate responses to voting queries

## Test 2: Members with suggested delay profiles
> [!NOTE]  
> Acceptor.java, Proposer.java and Paxosmember.java assumes no delay to members. Providing immediate responses.
> The delay is then integrated into the testing  
> The implementation of delay is as follow:  
>> M1 respond immediately, no delay.
>
>> M2 sometimes (< 30% of time) is at cafe, and 70% is at home where connection is largely delayed. This is  simulated using `SimulateLargeDelay(String memberId)`, where it delays 4 to 9 seconds.
>
>> M3 is not as slow as M2, but not as fast as M1. This is simulated using `simulateSmallDelay(String memberId)`
>> where it delays messages between 1 to 3 seconds.  
>> Sometimes, M3 is camping (< 30% of time), the message is dropped. Simulated using  `NoResponse()`.
>
>> M4 - M9 have busy schedule, so their delay is simulated using `simulateSmallDelay(String memberId)`, delaying between 1-3 seconds.

> ### Test 2.a: One proposer

