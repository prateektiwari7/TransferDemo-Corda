package com.template.states

import com.template.contracts.MergeState_Contract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.CommandAndState
import net.corda.core.contracts.OwnableState
import net.corda.core.identity.AbstractParty

@BelongsToContract(MergeState_Contract::class)
data class MergeState_State(val balance: Int,override val owner: AbstractParty) : OwnableState {

    override val participants get() = listOf(owner)

    override fun withNewOwner(newOwner: AbstractParty) = CommandAndState(MergeState_Contract.Commands.Move(),copy(owner= newOwner))


}
