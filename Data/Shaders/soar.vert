attribute vec3 a_position;

uniform float u_size;
uniform mat4 u_projectionView;
varying vec2 v_texCoords;

void main() {
    v_texCoords = vec2(a_position.x / u_size, a_position.y / u_size);
    gl_Position = u_projectionView * vec4(a_position, 1.0);
}