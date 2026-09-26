// SPDX-License-Identifier: Apache-2.0
// MediaTek Dimensity Multi-Cluster CPU Core Pinning for Xenon JIT
#include <pthread.h>
#include <sched.h>
#include <unistd.h>
#include <android/log.h>
#include <sys/system_properties.h>

#define TAG "XenDroid_Dimensity"

namespace xe::cpu::dimensity {

// MediaTek Dimensity Tri-Cluster Topology:
// Cores 0-3: Cortex-A510 (Efficiency, low freq)
// Cores 4-6: Cortex-A715 (Performance, ~2.8GHz)
// Core 7:    Cortex-X3 / X4 (Ultra Prime, ~3.25GHz+)

void PinThreadToPrimeCortexX(const char* thread_name) {
    cpu_set_t cpuset;
    CPU_ZERO(&cpuset);

    // Target the highest performance clusters (Cores 4, 5, 6, 7)
    // Core 7 is the Cortex-X Prime Core for JIT execution
    CPU_SET(6, &cpuset);
    CPU_SET(7, &cpuset);

    pthread_t current_thread = pthread_self();
    int rc = pthread_setaffinity_np(current_thread, sizeof(cpu_set_t), &cpuset);
    if (rc == 0) {
        __android_log_print(ANDROID_LOG_INFO, TAG,
            "Thread [%s] successfully pinned to Dimensity Cortex-X / A7xx cores (mask: 0xC0)", thread_name);
    } else {
        __android_log_print(ANDROID_LOG_WARN, TAG, "Failed to set CPU affinity: %d", rc);
    }

    // Set real-time audio/emulator scheduler priority
    struct sched_param param;
    param.sched_priority = sched_get_priority_max(SCHED_OTHER);
    pthread_setschedparam(current_thread, SCHED_OTHER, &param);
}

void PinWorkerThreadsToPerformanceCluster() {
    cpu_set_t cpuset;
    CPU_ZERO(&cpuset);
    // Cores 4 and 5 for Vulkan Command Buffer Dispatcher
    CPU_SET(4, &cpuset);
    CPU_SET(5, &cpuset);
    pthread_setaffinity_np(pthread_self(), sizeof(cpu_set_t), &cpuset);
}

} // namespace xe::cpu::dimensity
