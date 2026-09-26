// SPDX-License-Identifier: Apache-2.0
// Scissor Bounding & Tile Binner optimization for TBDR GPUs (Mali)

#include <vulkan/vulkan.h>
#include <algorithm>
#include <cstdint>

namespace xe::gpu::vulkan {

struct ScissorPatchState {
    uint32_t current_pass_union_x_ = 0;
    uint32_t current_pass_union_y_ = 0;
};

void CmdVkSetScissorMaliOptimized(
    ScissorPatchState& state,
    uint32_t first_scissor, uint32_t scissor_count,
    const VkRect2D* scissors, const VkRect2D& guest_viewport) {

    VkRect2D clamped_scissors[16];
    for (uint32_t i = 0; i < scissor_count && i < 16; ++i) {
        const auto& s = scissors[i];

        // Intersect guest scissor with visible viewport
        int32_t left   = std::max(s.offset.x, guest_viewport.offset.x);
        int32_t top    = std::max(s.offset.y, guest_viewport.offset.y);
        int32_t right  = std::min(s.offset.x + (int32_t)s.extent.width,
                                  guest_viewport.offset.x + (int32_t)guest_viewport.extent.width);
        int32_t bottom = std::min(s.offset.y + (int32_t)s.extent.height,
                                  guest_viewport.offset.y + (int32_t)guest_viewport.extent.height);

        clamped_scissors[i].offset.x = std::max(0, left);
        clamped_scissors[i].offset.y = std::max(0, top);
        clamped_scissors[i].extent.width  = (uint32_t)std::max(0, right - left);
        clamped_scissors[i].extent.height = (uint32_t)std::max(0, bottom - top);

        // Union into current pass dirty bounds so Mali TBDR doesn't allocate useless tiles
        state.current_pass_union_x_ = std::max(state.current_pass_union_x_,
            clamped_scissors[i].offset.x + clamped_scissors[i].extent.width);
        state.current_pass_union_y_ = std::max(state.current_pass_union_y_,
            clamped_scissors[i].offset.y + clamped_scissors[i].extent.height);
    }
}

} // namespace xe::gpu::vulkan
