#pragma once

struct Vector2 { float x, y; };
struct Vector3 { float x, y, z; };

// m: float[16] row-major, clip = M * (x,y,z,1).
// Origem da tela no canto superior esquerdo: Y e invertido.
inline bool WorldToScreen(const float m[16], const Vector3& p, int width, int height, Vector2* out) {
    float cx = m[0]  * p.x + m[1]  * p.y + m[2]  * p.z + m[3];
    float cy = m[4]  * p.x + m[5]  * p.y + m[6]  * p.z + m[7];
    float cw = m[12] * p.x + m[13] * p.y + m[14] * p.z + m[15];
    if (cw <= 0.001f) return false;

    float ndcX = cx / cw;
    float ndcY = cy / cw;
    out->x = (ndcX + 1.0f) * 0.5f * (float)width;
    out->y = (1.0f - ndcY) * 0.5f * (float)height;
    return true;
}
