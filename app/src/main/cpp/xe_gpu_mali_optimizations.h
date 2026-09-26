// SPDX-License-Identifier: Apache-2.0
// XenDroid Mali Optimization Header: ARM Valhall & 5th Gen Tuning
#pragma once

#include <vulkan/vulkan.h>
#include <cstdint>
#include <string>

namespace xe::gpu::vulkan {

constexpr uint32_t VENDOR_ARM_MALI = 0x13B5;
constexpr uint32_t VENDOR_QUALCOMM = 0x5143;

struct MaliGpuFeatures {
    bool is_arm_mali = false;
    bool supports_fp16_math = false;
    bool requires_subgroup_16 = false;
    bool enable_tbdr_pass_fusion = true;
    uint32_t subgroup_size = 16;
    std::string device_name;
};

class MaliGpuTuner {
public:
    static MaliGpuFeatures ProbeAndConfigure(VkPhysicalDevice physical_device) {
        VkPhysicalDeviceProperties props;
        vkGetPhysicalDeviceProperties(physical_device, &props);

        MaliGpuFeatures feat;
        feat.device_name = props.deviceName;
        feat.is_arm_mali = (props.vendorID == VENDOR_ARM_MALI);

        if (feat.is_arm_mali) {
            // Mali Valhall / 5th Gen (G710, G715, G720) uses 16-wide or 32-wide warp waves
            feat.subgroup_size = 16;
            feat.requires_subgroup_16 = true;
            feat.supports_fp16_math = true;
            feat.enable_tbdr_pass_fusion = true;
        }
        return feat;
    }

    // Force TBDR attachments to avoid memory flush on tile edges
    static VkAttachmentDescription ConfigureMaliAttachment(VkFormat format) {
        VkAttachmentDescription desc = {};
        desc.format = format;
        desc.samples = VK_SAMPLE_COUNT_1_BIT;
        desc.loadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE; // Crucial for Mali TBDR!
        desc.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
        desc.initialLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
        desc.finalLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
        return desc;
    }
};

} // namespace xe::gpu::vulkan
