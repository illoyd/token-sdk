package com.r3.corda.sdk.token.workflow

import com.r3.corda.sdk.token.contracts.types.TokenPointer
import com.r3.corda.sdk.token.contracts.utilities.of
import com.r3.corda.sdk.token.money.GBP
import com.r3.corda.sdk.token.workflow.statesAndContracts.House
import com.r3.corda.sdk.token.workflow.utilities.getDistributionList
import com.r3.corda.sdk.token.workflow.utilities.getLinearStateById
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.StartedMockNode
import org.junit.Before
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals

class TokenFlowTests : MockNetworkTest(numberOfNodes = 3) {

    lateinit var A: StartedMockNode
    lateinit var B: StartedMockNode
    lateinit var I: StartedMockNode

    @Before
    override fun initialiseNodes() {
        A = nodes[0]
        B = nodes[1]
        I = nodes[2]
    }

    @Test
    fun `create evolvable token`() {
        // Create new token.
        val house = House("24 Leinster Gardens, Bayswater, London", 1_000_000.GBP, listOf(I.legalIdentity()))
        val tx = I.createEvolvableToken(house, NOTARY.legalIdentity()).getOrThrow()
        val token = tx.singleOutput<House>()
        assertEquals(house, token.state.data)
    }

    @Test
    fun `create and update evolvable token`() {
        // Create new token.
        val house = House("24 Leinster Gardens, Bayswater, London", 1_000_000.GBP, listOf(I.legalIdentity()))
        val tx = I.createEvolvableToken(house, NOTARY.legalIdentity()).getOrThrow()
        val oldToken = tx.singleOutput<House>()
        // Propose an update to the token.
        val proposedToken = house.copy(valuation = 950_000.GBP)
        val updateTx = I.updateEvolvableToken(oldToken, proposedToken).getOrThrow()
        val newToken = updateTx.singleOutput<House>()
        assertEquals(proposedToken, newToken.state.data)
    }

    @Test
    fun `issue and move fixed tokens`() {
        println("Issuing")
        val issueTokenTx = I.issueTokens(GBP, A, NOTARY, 100.GBP).getOrThrow()
        println("Issuing: $issueTokenTx")
        A.watchForTransaction(issueTokenTx.id).getOrThrow(Duration.ofSeconds(5))
        println("Moving")
        val moveTokenTx = A.moveTokens(GBP, B, 50.GBP, anonymous = true).getOrThrow()
        println("Moving: $moveTokenTx")
        B.watchForTransaction(moveTokenTx.id).getOrThrow(Duration.ofSeconds(5))
        println(moveTokenTx.tx)
    }

    @Test
    fun `create evolvable token, then issue evolvable token`() {
        // Create new token.
        val house = House("24 Leinster Gardens, Bayswater, London", 1_000_000.GBP, listOf(I.legalIdentity()))
        val createTokenTx = I.createEvolvableToken(house, NOTARY.legalIdentity()).getOrThrow()
        val token = createTokenTx.singleOutput<House>()
        // Issue amount of the token.
        val housePointer: TokenPointer<House> = house.toPointer()
        val issueTokenAmountTx = I.issueTokens(housePointer, A, NOTARY, 100 of housePointer).getOrThrow()
        A.watchForTransaction(issueTokenAmountTx.id).getOrThrow()
        // Check to see that A was added to I's distribution list.
        val distributionList = I.transaction { getDistributionList(I.services, token.linearId()) }
        val distributionRecord = distributionList.single()
        assertEquals(A.legalIdentity(), distributionRecord.party)
        assertEquals(token.linearId().id, distributionRecord.linearId)
    }

    @Test
    fun `check token recipient also receives token`() {
        // Create new token.
        val house = House("24 Leinster Gardens, Bayswater, London", 1_000_000.GBP, listOf(I.legalIdentity()))
        val createTokenTx = I.createEvolvableToken(house, NOTARY.legalIdentity()).getOrThrow()
        val houseToken: StateAndRef<House> = createTokenTx.singleOutput()
        // Issue amount of the token.
        val housePointer: TokenPointer<House> = house.toPointer()
        val issueTokenAmountTx = I.issueTokens(housePointer, A, NOTARY, 100 of housePointer).getOrThrow()
        // Wait for the transaction to be recorded by A.
        A.watchForTransaction(issueTokenAmountTx.id).getOrThrow()
        val houseQuery = A.services.vaultService.queryBy<House>().states
        assertEquals(houseToken, houseQuery.single())
    }

    @Test
    fun `create evolvable token, then issue tokens, then update token`() {
        // Create new token.
        val house = House("24 Leinster Gardens, Bayswater, London", 1_000_000.GBP, listOf(I.legalIdentity()))
        val createTokenTx = I.createEvolvableToken(house, NOTARY.legalIdentity()).getOrThrow()
        val token = createTokenTx.singleOutput<House>()
        // Issue amount of the token.
        val housePointer: TokenPointer<House> = house.toPointer()
        val issueTokenAmountTx = I.issueTokens(housePointer, A, NOTARY, 100 of housePointer).getOrThrow()
        A.watchForTransaction(issueTokenAmountTx.id).toCompletableFuture().getOrThrow()
        // Update the token.
        val proposedToken = house.copy(valuation = 950_000.GBP)
        val updateTx = I.updateEvolvableToken(token, proposedToken).getOrThrow()
        // Wait to see if A is sent the updated token.
        val updatedToken = A.watchForTransaction(updateTx.id).toCompletableFuture().getOrThrow()
        assertEquals(updateTx.singleOutput(), updatedToken.singleOutput<House>())
    }

    @Test
    fun `create evolvable token, then issue and move`() {
        // Create new token.
        val house = House("24 Leinster Gardens, Bayswater, London", 1_000_000.GBP, listOf(I.legalIdentity()))
        val createTokenTx = I.createEvolvableToken(house, NOTARY.legalIdentity()).getOrThrow()
        // Issue amount of the token.
        val housePointer: TokenPointer<House> = house.toPointer()
        val issueTokenTx = I.issueTokens(housePointer, A, NOTARY, 100 of housePointer).getOrThrow()
        A.watchForTransaction(issueTokenTx.id).toCompletableFuture().getOrThrow()
        val houseQuery = A.transaction { A.services.vaultService.getLinearStateById<House>(housePointer.pointer.pointer) }
        assertEquals(housePointer.pointer.pointer, houseQuery?.state?.data?.linearId, "Did not find the expected linear ID")
        // Move some of the tokens.
        val moveTokenTx = A.moveTokens(housePointer, B, 50 of housePointer, anonymous = true).getOrThrow()
        B.watchForTransaction(moveTokenTx.id).getOrThrow()
    }

    @Test
    fun `create evolvable token and issue to multiple nodes`() {
        // Create new token.
        val house = House("24 Leinster Gardens, Bayswater, London", 1_000_000.GBP, listOf(I.legalIdentity()))
        val housePointer: TokenPointer<House> = house.toPointer()
        val tx = I.createEvolvableToken(house, NOTARY.legalIdentity()).getOrThrow()
        val token = tx.singleOutput<House>()
        assertEquals(house, token.state.data)
        // Issue to node A.
        val issueTokenA = I.issueTokens(housePointer, A, NOTARY, 50 of housePointer).getOrThrow()
        A.watchForTransaction(issueTokenA.id).toCompletableFuture().getOrThrow()
        // Issue to node B.
        val issueTokenB = I.issueTokens(housePointer, B, NOTARY, 50 of housePointer).getOrThrow()
        B.watchForTransaction(issueTokenB.id).toCompletableFuture().getOrThrow()
    }

}