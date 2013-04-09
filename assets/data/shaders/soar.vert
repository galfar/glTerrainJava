attribute vec3 position;

uniform float terrainSize;
uniform mat4 matProjView;

varying vec2 groundTexCoords;
varying vec2 detailTexCoords;

const float detailScale = 0.25;

void main() {
    groundTexCoords = vec2(position.x / terrainSize, position.y / terrainSize);
    detailTexCoords = vec2(position.x * detailScale, position.y * detailScale);
    gl_Position = matProjView * vec4(position, 1.0);
}