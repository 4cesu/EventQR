package com.thedavelopers.eventqr.core.util

import com.thedavelopers.eventqr.R
import com.thedavelopers.eventqr.core.api.dto.AccountRole

/**
 * Single source of truth for the portal-switcher feature.
 *
 * Every user always has the Attendee Portal. Users additionally hold exactly the portal
 * matching the single role assigned to their account. Roles are NOT cumulative: an ADMIN
 * only sees Admin Portal (+ Attendee Portal), a SUPER_ADMIN only sees
 * Super Admin Portal (+ Attendee Portal), a STAFF only sees Staff Portal (+ Attendee Portal),
 * and a plain attendee sees only the Attendee Portal (the switcher stays hidden).
 */
object PortalSwitcher {
    const val PORTAL_ATTENDEE = "Attendee Portal"
    const val PORTAL_STAFF = "Staff Portal"
    const val PORTAL_ORGANIZER = "Organizer Portal"
    const val PORTAL_ADMIN = "Admin Portal"
    const val PORTAL_SUPER_ADMIN = "Super Admin Portal"

    /**
     * The portals a user with the given [normalizedRole] (see [RoleMapper.normalizeRole])
     * may switch to. Any role that is not one of the elevated roles yields the Attendee
     * portal only, so off-app or unexpected role values can never leak elevated portals.
     */
    fun portalsForRole(normalizedRole: String?): List<String> = buildList {
        add(PORTAL_ATTENDEE)
        when (normalizedRole) {
            AccountRole.STAFF.name -> add(PORTAL_STAFF)
            AccountRole.ORGANIZER.name -> add(PORTAL_ORGANIZER)
            AccountRole.ADMIN.name -> add(PORTAL_ADMIN)
            AccountRole.SUPER_ADMIN.name -> add(PORTAL_SUPER_ADMIN)
        }
    }

    /** Row icon for a portal option. */
    fun iconRes(portal: String): Int = when (portal) {
        PORTAL_ATTENDEE -> R.drawable.ic_nav_profile
        PORTAL_STAFF -> R.drawable.ic_qr_scan
        PORTAL_ORGANIZER -> R.drawable.ic_calendar
        PORTAL_ADMIN, PORTAL_SUPER_ADMIN -> R.drawable.ic_group
        else -> R.drawable.ic_nav_home
    }

    /** Row subtitle for a portal option. */
    fun subtitle(portal: String): String = when (portal) {
        PORTAL_ATTENDEE -> "Events, rewards, and your profile"
        PORTAL_STAFF -> "Scan QR codes and manage entries"
        PORTAL_ORGANIZER -> "Manage your events and attendees"
        PORTAL_ADMIN -> "Platform administration and oversight"
        PORTAL_SUPER_ADMIN -> "Full platform administration and control"
        else -> "Open portal"
    }
}