//#define DRAW_EDGES

precision mediump float;

uniform float nodeSize;
uniform sampler2D texGround;
uniform sampler2D texDetail;

varying vec2 groundTexCoords;
varying vec2 detailTexCoords;

#ifdef DRAW_EDGES
#extension GL_OES_standard_derivatives : enable

varying vec3 baryCoords;
varying vec2 nodeCoords;

const vec3 colorTriEdge = vec3(0.5, 0.7, 0.9);
const vec3 colorNodeEdge = vec3(1.0, 0.0, 0.0);
const float triEdgeWidth = 1.0;
const float nodeEdgeWidth = 2.5;

float calcTriEdgeFactor() {
    vec3 d = fwidth(baryCoords);
    vec3 a3 = smoothstep(vec3(0.0), d * triEdgeWidth, baryCoords);
    return min(min(a3.x, a3.y), a3.z);    
}

float calcNodeEdgeFactor() {    
    vec2 d = fwidth(nodeCoords);    
    vec2 a1 = smoothstep(vec2(0.0), d * nodeEdgeWidth, nodeCoords);
    vec2 a2 = smoothstep(vec2(0.0), d * nodeEdgeWidth, 1.0 - nodeCoords);    
    return min(min(a1.x, a1.y), min(a2.x, a2.y));  
}
#endif

void main() {
    vec3 ground = texture2D(texGround, groundTexCoords).rgb;
    float detail = clamp(texture2D(texDetail, detailTexCoords).a - 0.5, -0.1, 0.1);    
    vec3 color = ground + vec3(detail);

#ifdef DRAW_EDGES    
    float triFactor = calcTriEdgeFactor();
    float nodeFactor = calcNodeEdgeFactor();
    
    if (nodeFactor < 0.4)
        triFactor = 1.0;
    
    vec3 triEdgeColor = mix(colorTriEdge, vec3(0.0), triFactor);
    vec3 nodeEdgeColor = mix(colorNodeEdge, vec3(0.0), nodeFactor);
    color = color * clamp(nodeFactor + triFactor, 0.0, 1.0) + nodeEdgeColor + triEdgeColor;
#endif

    gl_FragColor.rgb = color;     
}