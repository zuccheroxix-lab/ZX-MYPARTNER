package com.example.data

data class ProjectItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val version: String,
    val category: String,
    val status: String,
    val stars: String,
    val language: String,
    val tags: List<String>,
    val url: String
)

object ProjectRepository {
    val projects: List<ProjectItem> = listOf(
        ProjectItem(
            id = "zx_dashboard",
            title = "ZX DASHBOARD",
            subtitle = "Native Android Dashboard & System Monitor",
            description = "A high-performance, dark-themed native Android control center delivering real-time hardware diagnostics, battery telemetries, sensor scanner, and network tools.",
            version = "v1.0.0",
            category = "Android Native",
            status = "Active",
            stars = "128",
            language = "Kotlin / Compose",
            tags = listOf("Kotlin", "Jetpack Compose", "Hardware API", "Material 3"),
            url = "https://github.com/zuccheroxann/zx-dashboard"
        ),
        ProjectItem(
            id = "zx_mod_toolkit",
            title = "ZX Mod Toolkit",
            subtitle = "Advanced Android System Utility & Customization Suite",
            description = "Comprehensive utility toolkit for power users providing build information inspectors, overlay display helpers, logcat filters, and package management tools.",
            version = "v2.4.2",
            category = "System Tools",
            status = "Active",
            stars = "245",
            language = "Kotlin / Compose",
            tags = listOf("System Diagnostics", "ADB Utils", "System Tweaks"),
            url = "https://github.com/zuccheroxann/zx-mod-toolkit"
        ),
        ProjectItem(
            id = "zx_kernel_tweaker",
            title = "ZX Kernel Tweaker",
            subtitle = "Governor & CPU Frequency Profile Optimizer",
            description = "Lightweight governor tuner allowing fine-grained scheduler controls, CPU core state inspection, GPU throttling mitigation, and battery preservation modes.",
            version = "v3.1.0",
            category = "Kernel / Low-Level",
            status = "Stable",
            stars = "389",
            language = "C / Shell / Kotlin",
            tags = listOf("Kernel", "CPU Governor", "Battery Optimization"),
            url = "https://github.com/zuccheroxann/zx-kernel-tweaker"
        ),
        ProjectItem(
            id = "zx_cyber_suite",
            title = "ZX Cyber Security Suite",
            subtitle = "Network Traffic & Integrity Analyzer",
            description = "Network diagnostic assistant, DNS latency tester, ping analyzer, SSL certificate validator, and Wi-Fi connection auditor.",
            version = "v1.5.0",
            category = "Security & Network",
            status = "Active",
            stars = "192",
            language = "Kotlin / Go",
            tags = listOf("Network", "DNS", "Security", "SSL"),
            url = "https://github.com/zuccheroxann/zx-cyber-suite"
        ),
        ProjectItem(
            id = "zx_rom_customizer",
            title = "ZX ROM Customizer",
            subtitle = "Custom Android ROM Addon & Theming Assistant",
            description = "Unified engine for applying system icon packs, font overrides, boot animations, status bar clock repositioning, and dynamic accent engines on AOSP.",
            version = "v4.0.1",
            category = "Customization",
            status = "Stable",
            stars = "512",
            language = "Kotlin / Java",
            tags = listOf("AOSP", "Theming", "Substratum", "Icon Engine"),
            url = "https://github.com/zuccheroxann/zx-rom-customizer"
        ),
        ProjectItem(
            id = "zx_bot_automation",
            title = "ZX Bot Automation",
            subtitle = "Telegram & Multi-Platform Server Management Bot",
            description = "Cloud server monitoring daemon that sends instant downtime alerts, manages automated health checks, triggers backup snapshots, and delivers status digests.",
            version = "v2.0.8",
            category = "Cloud & Automation",
            status = "Active",
            stars = "167",
            language = "Python / Docker",
            tags = listOf("Telegram Bot", "DevOps", "Monitoring", "Automation"),
            url = "https://github.com/zuccheroxann/zx-bot-automation"
        )
    )
}
