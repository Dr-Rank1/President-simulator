package com.presidentsimulator.game.data

import kotlinx.serialization.Serializable

@Serializable
enum class ExecutiveOrderType(
    val title: String,
    val description: String,
    val monthlyCostMillion: Long,
    val approvalImpact: Float,
) {
    EMERGENCY_ECONOMIC_STIMULUS(
        title = "National Economic Stimulus Package",
        description = "Inject emergency liquidity into local businesses and small manufacturers.",
        monthlyCostMillion = 800L,
        approvalImpact = 5.0f,
    ),
    NATIONAL_DEFENSE_MOBILIZATION(
        title = "Strategic Defense Modernization Order",
        description = "Accelerate arms production lines and increase military readiness drill cadence.",
        monthlyCostMillion = 650L,
        approvalImpact = 3.5f,
    ),
    WORKERS_MINIMUM_WAGE_DECREE(
        title = "Living Wage & Fair Labor Decree",
        description = "Establish elevated federal wage floors to avert nationwide industrial strikes.",
        monthlyCostMillion = 450L,
        approvalImpact = 6.0f,
    ),
    ENVIRONMENTAL_GREEN_TRANSITION(
        title = "Clean Skies & Water Renewal Mandate",
        description = "Mandate industrial emission scrubbers and expand municipal water treatment.",
        monthlyCostMillion = 380L,
        approvalImpact = 4.0f,
    ),
    STATE_SECURITY_CRACKDOWN(
        title = "Organized Crime & Corruption Crackdown",
        description = "Authorize special prosecutor joint taskforces and SWAT raids on criminal cartels.",
        monthlyCostMillion = 290L,
        approvalImpact = 4.5f,
    ),
    PRO_NATALIST_CHILD_SUBSIDY(
        title = "Family Prosperity & Child Allowance Act",
        description = "Monthly parental stipend for 2nd and 3rd children to stimulate population growth.",
        monthlyCostMillion = 520L,
        approvalImpact = 5.5f,
    ),
    TRADE_EMBARGO_RIVALS(
        title = "Strategic Resource Export Restriction",
        description = "Block rare mineral and defense hardware shipments to hostile rival powers.",
        monthlyCostMillion = 200L,
        approvalImpact = 2.0f,
    ),
}

@Serializable
data class PresidentialDecree(
    val type: ExecutiveOrderType,
    val isEnacted: Boolean = false,
    val enactedYear: Int? = null,
)

@Serializable
enum class VoterDemographic(
    val title: String,
    val description: String,
    val weightPercent: Float,
) {
    BLUE_COLLAR_WORKERS(
        title = "Industrial & Agricultural Labor",
        description = "Factory machinists, miners, and farm workers seeking fair pay and job security.",
        weightPercent = 0.35f,
    ),
    BUSINESS_EXECUTIVES(
        title = "Entrepreneurs & Corporate Leaders",
        description = "Tech founders, industrialists, and investors prioritizing low taxes and free trade.",
        weightPercent = 0.20f,
    ),
    MILITARY_VETERANS(
        title = "Armed Forces & Veterans",
        description = "Active personnel and retirees demanding strong defense budgets and border security.",
        weightPercent = 0.15f,
    ),
    CIVIL_SERVANTS(
        title = "Public Sector & Educators",
        description = "Teachers, civil servants, and healthcare staff relying on stable government funding.",
        weightPercent = 0.18f,
    ),
    YOUTH_STUDENTS(
        title = "University Students & Youth",
        description = "Young voters passionate about digital technology, university grants, and ecology.",
        weightPercent = 0.12f,
    ),
}

@Serializable
data class DemographicPolling(
    val approvalByDemographic: Map<VoterDemographic, Float> = defaultApproval(),
) {
    val overallWeightedApproval: Float
        get() = VoterDemographic.entries.sumOf { (approvalByDemographic[it]?.toDouble() ?: 50.0) * it.weightPercent.toDouble() }.toFloat()

    companion object {
        fun defaultApproval(): Map<VoterDemographic, Float> = mapOf(
            VoterDemographic.BLUE_COLLAR_WORKERS to 62.0f,
            VoterDemographic.BUSINESS_EXECUTIVES to 58.0f,
            VoterDemographic.MILITARY_VETERANS to 74.0f,
            VoterDemographic.CIVIL_SERVANTS to 66.0f,
            VoterDemographic.YOUTH_STUDENTS to 55.0f,
        )
    }
}

@Serializable
data class OvalOfficeState(
    val decrees: Map<ExecutiveOrderType, PresidentialDecree> = ExecutiveOrderType.entries.associateWith {
        PresidentialDecree(type = it, isEnacted = false)
    },
    val polling: DemographicPolling = DemographicPolling(),
    val deskHotlineRinging: Boolean = false,
    val resoluteDeskStyle: String = "Classic Oval Mahogany",
) {
    val activeDecreesCount: Int get() = decrees.values.count { it.isEnacted }
    val monthlyDecreesCostMillion: Long
        get() = decrees.values.filter { it.isEnacted }.sumOf { it.type.monthlyCostMillion }
}
