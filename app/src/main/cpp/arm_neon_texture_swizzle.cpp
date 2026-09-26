// SPDX-License-Identifier: Apache-2.0
// ARM NEON Accelerated Xbox 360 Texture Untiling & Byte Swapping

#if defined(__ARM_NEON) || defined(__aarch64__)
#include <arm_neon.h>
#include <cstddef>
#include <cstdint>

namespace xe::gpu {

void UntileTextureBlock128Neon(uint8_t* dst, const uint8_t* src, size_t num_quadwords) {
    size_t i = 0;
    // Unrolled 64-byte burst copy with Endianness byte flip
    for (; i + 4 <= num_quadwords; i += 4) {
        uint8x16_t q0 = vld1q_u8(src + (i + 0) * 16);
        uint8x16_t q1 = vld1q_u8(src + (i + 1) * 16);
        uint8x16_t q2 = vld1q_u8(src + (i + 2) * 16);
        uint8x16_t q3 = vld1q_u8(src + (i + 3) * 16);

        // Xbox 360 PowerPC Big Endian -> ARMv8/v9 Little Endian 32-bit swap
        uint32x4_t swapped0 = vrev32q_u32(vreinterpretq_u32_u8(q0));
        uint32x4_t swapped1 = vrev32q_u32(vreinterpretq_u32_u8(q1));
        uint32x4_t swapped2 = vrev32q_u32(vreinterpretq_u32_u8(q2));
        uint32x4_t swapped3 = vrev32q_u32(vreinterpretq_u32_u8(q3));

        vst1q_u8(dst + (i + 0) * 16, vreinterpretq_u8_u32(swapped0));
        vst1q_u8(dst + (i + 1) * 16, vreinterpretq_u8_u32(swapped1));
        vst1q_u8(dst + (i + 2) * 16, vreinterpretq_u8_u32(swapped2));
        vst1q_u8(dst + (i + 3) * 16, vreinterpretq_u8_u32(swapped3));
    }

    // Remainder loop
    for (; i < num_quadwords; ++i) {
        uint8x16_t q = vld1q_u8(src + i * 16);
        uint32x4_t swapped = vrev32q_u32(vreinterpretq_u32_u8(q));
        vst1q_u8(dst + i * 16, vreinterpretq_u8_u32(swapped));
    }
}

} // namespace xe::gpu
#endif
